package qc.levis.contrat.clm.application;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;
import qc.levis.contrat.clm.application.dto.ClmDto;
import qc.levis.contrat.clm.application.repository.EtapeTrousseRepository;
import qc.levis.contrat.clm.application.repository.EvenementTrousseRepository;
import qc.levis.contrat.clm.application.repository.InvitationRepository;
import qc.levis.contrat.clm.application.repository.MessageSortantRepository;
import qc.levis.contrat.clm.application.repository.TrousseRepository;
import qc.levis.contrat.clm.domain.EtapeTrousse;
import qc.levis.contrat.clm.domain.EvenementTrousse;
import qc.levis.contrat.clm.domain.Habilitations;
import qc.levis.contrat.clm.domain.Invitation;
import qc.levis.contrat.clm.domain.MessageSortant;
import qc.levis.contrat.clm.domain.Parcours;
import qc.levis.contrat.clm.domain.Relances;
import qc.levis.contrat.clm.domain.StatutTrousse;
import qc.levis.contrat.clm.domain.Trousse;

/** Pilotage (ST-13, UI-01, UI-18) : tableau de bord et suivi multi-trousses limité au périmètre de l'acteur. */
@Service
public class SuiviService {

    public static final String BLOCAGE = "BLOCAGE";
    public static final String ERREUR_SALESFORCE = "ERREUR_SALESFORCE";
    public static final String SIGNATURE_EXPIREE = "SIGNATURE_EXPIREE";

    public record Filtre(String q, String statut, String unite, String responsable, String source, LocalDate du, LocalDate au,
                         boolean exceptionsSeulement) {
    }

    private final TrousseRepository trousses;
    private final EtapeTrousseRepository etapes;
    private final EvenementTrousseRepository evenements;
    private final InvitationRepository invitations;
    private final MessageSortantRepository sorties;
    private final ParcoursService parcours;
    private final LectureTrousse lecture;
    private final ContexteClm ctx;

    public SuiviService(TrousseRepository trousses, EtapeTrousseRepository etapes, EvenementTrousseRepository evenements,
                        InvitationRepository invitations, MessageSortantRepository sorties, ParcoursService parcours,
                        LectureTrousse lecture, ContexteClm ctx) {
        this.trousses = trousses;
        this.etapes = etapes;
        this.evenements = evenements;
        this.invitations = invitations;
        this.sorties = sorties;
        this.parcours = parcours;
        this.lecture = lecture;
        this.ctx = ctx;
    }

    public List<ClmDto.SuiviLigne> suivi(Habilitations.Acteur acteur, Filtre f) {
        Instant maintenant = ctx.maintenant();
        List<ClmDto.SuiviLigne> res = new ArrayList<>();
        for (Trousse t : visibles(acteur)) {
            if (!retenue(t, f)) {
                continue;
            }
            ClmDto.SuiviLigne l = ligne(t, maintenant);
            if (!f.exceptionsSeulement() || !l.exceptions().isEmpty()) {
                res.add(l);
            }
        }
        return res;
    }

    private boolean retenue(Trousse t, Filtre f) {
        LocalDate maj = LocalDate.ofInstant(t.getMajLe(), ZoneOffset.UTC);
        return (vide(f.statut()) || t.getStatut().name().equals(f.statut()))
                && (vide(f.unite()) || f.unite().equals(t.getUnite()))
                && (vide(f.responsable()) || t.getProprietaire().equalsIgnoreCase(f.responsable()))
                && (vide(f.source()) || t.getSource().name().equals(f.source()))
                && (f.du() == null || !maj.isBefore(f.du()))
                && (f.au() == null || !maj.isAfter(f.au()))
                && (vide(f.q()) || texte(t).contains(f.q().trim().toLowerCase(Locale.ROOT)));
    }

    private static String texte(Trousse t) {
        return (t.getReference() + " " + t.getTitre() + " " + (t.getSfDossierId() == null ? "" : t.getSfDossierId()))
                .toLowerCase(Locale.ROOT);
    }

    ClmDto.SuiviLigne ligne(Trousse t, Instant maintenant) {
        List<EtapeTrousse> actives = parcours.etapesDe(t).stream().filter(e -> e.getStatut() == Parcours.StatutEtape.ACTIVE).toList();
        List<String> attente = new ArrayList<>();
        actives.forEach(e -> ctx.affectations(e.getAffectationsJson()).forEach(a -> attente.add(a.type().name().toLowerCase() + ":" + a.valeur())));
        Instant echeance = actives.stream().map(EtapeTrousse::getEcheance).filter(java.util.Objects::nonNull).min(Comparator.naturalOrder()).orElse(null);
        List<String> exceptions = new ArrayList<>();
        if (actives.stream().anyMatch(e -> Relances.enRetard(e.getEcheance(), maintenant))) {
            exceptions.add(BLOCAGE);
        }
        if (sorties.findByTenantIdAndTrousseIdInAndStatut(t.getTenantId(), List.of(t.getId()), MessageSortant.Statut.ECHEC).size() > 0) {
            exceptions.add(ERREUR_SALESFORCE);
        }
        boolean expiree = invitations.findByTenantIdAndTrousseIdOrderByIdAsc(t.getTenantId(), t.getId()).stream()
                .anyMatch(i -> (i.getStatut() == Invitation.Statut.ENVOYEE || i.getStatut() == Invitation.Statut.VERIFIEE) && i.estExpiree(maintenant));
        if (expiree) {
            exceptions.add(SIGNATURE_EXPIREE);
        }
        EvenementTrousse dernier = evenements.findTopByTenantIdAndTrousseIdOrderBySequenceDesc(t.getTenantId(), t.getId()).orElse(null);
        return new ClmDto.SuiviLigne(t.getId(), t.getReference(), t.getTitre(), t.getStatut().name(), t.getSource().name(), t.getUnite(),
                t.getProprietaire(), actives.stream().map(EtapeTrousse::getLibelle).toList(), attente, echeance, exceptions.contains(BLOCAGE),
                dernier == null ? null : dernier.getType(), dernier == null ? null : dernier.getHorodatage(), exceptions);
    }

    /** Export CSV (UI-18) ; les cellules commençant par un caractère de formule sont neutralisées. */
    public String exporterCsv(Habilitations.Acteur acteur, Filtre f) {
        StringBuilder sb = new StringBuilder("reference;titre;statut;source;unite;responsable;etapes_actives;echeance;exceptions\n");
        for (ClmDto.SuiviLigne l : suivi(acteur, f)) {
            sb.append(String.join(";", cellule(l.reference()), cellule(l.titre()), l.statut(), l.source(), cellule(l.unite()),
                    cellule(l.proprietaire()), cellule(String.join(" | ", l.etapesActives())),
                    l.echeance() == null ? "" : l.echeance().toString(), String.join(" | ", l.exceptions()))).append('\n');
        }
        return sb.toString();
    }

    static String cellule(String v) {
        if (v == null) {
            return "";
        }
        String s = v.replace("\"", "\"\"").replace('\n', ' ');
        if (!s.isEmpty() && "=+-@\t\r".indexOf(s.charAt(0)) >= 0) {
            s = "'" + s;
        }
        return "\"" + s + "\"";
    }

    public ClmDto.TableauBord tableauBord(Habilitations.Acteur acteur, Filtre f) {
        Instant maintenant = ctx.maintenant();
        List<Trousse> visibles = visibles(acteur).stream().filter(t -> retenue(t, f)).toList();
        Map<String, Long> ind = new LinkedHashMap<>();
        ind.put("brouillons", visibles.stream().filter(t -> t.getStatut() == StatutTrousse.BROUILLON).count());
        long validation = 0;
        long signature = 0;
        long retard = 0;
        List<Long> ids = visibles.stream().map(Trousse::getId).toList();
        List<EtapeTrousse> actives = ids.isEmpty() ? List.of()
                : etapes.findByTenantIdAndTrousseIdInAndStatut(ctx.tenant(), ids, Parcours.StatutEtape.ACTIVE);
        for (Long id : ids) {
            List<EtapeTrousse> lesActives = actives.stream().filter(e -> e.getTrousseId().equals(id)).toList();
            if (lesActives.stream().anyMatch(e -> e.getType() != Parcours.TypeEtape.SIGNATURE)) validation++;
            if (lesActives.stream().anyMatch(e -> e.getType() == Parcours.TypeEtape.SIGNATURE)) signature++;
            if (lesActives.stream().anyMatch(e -> Relances.enRetard(e.getEcheance(), maintenant))) retard++;
        }
        ind.put("enValidation", validation);
        ind.put("enSignature", signature);
        ind.put("enRetard", retard);
        ind.put("terminees", visibles.stream().filter(t -> t.getStatut() == StatutTrousse.TERMINEE).count());
        List<ClmDto.EvenementVue> activite = evenements.findTop30ByTenantIdOrderByHorodatageDesc(ctx.tenant()).stream()
                .filter(e -> ids.contains(e.getTrousseId())).limit(10).map(LectureTrousse::evenement).toList();
        List<ClmDto.SortieVue> alertes = ids.isEmpty() ? List.of()
                : sorties.findByTenantIdAndTrousseIdInAndStatut(ctx.tenant(), ids, MessageSortant.Statut.ECHEC).stream()
                .map(SalesforceService::vue).toList();
        return new ClmDto.TableauBord(ind, parcours.mesTaches(acteur, "a_traiter"), activite, alertes);
    }

    private List<Trousse> visibles(Habilitations.Acteur acteur) {
        return trousses.findByTenantIdOrderByMajLeDesc(ctx.tenant()).stream().filter(t -> lecture.visible(t, acteur)).toList();
    }

    // --- Journal d'audit et événements (UI-23) ---
    public record FiltreJournal(LocalDate du, LocalDate au, String acteur, String action, String trousse, String texte) {
    }

    static final int PERIODE_MAX_JOURS = 366;

    /** Événements scellés du tenant, du plus récent au plus ancien ; la période est bornée pour rester exploitable. */
    public List<ClmDto.JournalLigne> journal(FiltreJournal f) {
        LocalDate au = f.au() == null ? LocalDate.ofInstant(ctx.maintenant(), ZoneOffset.UTC) : f.au();
        LocalDate du = f.du() == null ? au.minusDays(30) : f.du();
        if (du.isAfter(au)) {
            throw new IllegalArgumentException("Période invalide : la date de début suit la date de fin.");
        }
        if (java.time.temporal.ChronoUnit.DAYS.between(du, au) > PERIODE_MAX_JOURS) {
            throw new IllegalArgumentException("Période trop large : " + PERIODE_MAX_JOURS + " jours au plus.");
        }
        Map<Long, String> references = new java.util.HashMap<>();
        trousses.findByTenantIdOrderByMajLeDesc(ctx.tenant()).forEach(t -> references.put(t.getId(), t.getReference()));
        List<ClmDto.JournalLigne> res = new ArrayList<>();
        for (EvenementTrousse e : evenements.findByTenantIdAndHorodatageBetweenOrderByHorodatageDesc(ctx.tenant(),
                du.atStartOfDay().toInstant(ZoneOffset.UTC), au.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC))) {
            String ref = references.getOrDefault(e.getTrousseId(), "#" + e.getTrousseId());
            if (contient(e.getActeur(), f.acteur()) && contient(e.getType(), f.action()) && contient(ref, f.trousse())
                    && (vide(f.texte()) || contient(e.getDetail(), f.texte()) || contient(e.getEmpreinte(), f.texte()))) {
                res.add(new ClmDto.JournalLigne(e.getTrousseId(), ref, e.getSequence(), e.getType(), e.getActeur(), e.getHorodatage(),
                        e.getDetail(), "systeme".equals(e.getActeur()), e.getEmpreinte()));
            }
        }
        return res;
    }

    public String journalCsv(FiltreJournal f) {
        StringBuilder sb = new StringBuilder("horodatage;trousse;sequence;evenement;acteur;origine;detail;empreinte\n");
        for (ClmDto.JournalLigne l : journal(f)) {
            sb.append(String.join(";", l.horodatage().toString(), cellule(l.reference()), Long.toString(l.sequence()), l.type(),
                    cellule(l.acteur()), l.systeme() ? "SYSTEME" : "METIER", cellule(l.detail()), l.empreinte())).append('\n');
        }
        return sb.toString();
    }

    private static boolean contient(String valeur, String recherche) {
        return vide(recherche) || (valeur != null && valeur.toLowerCase(Locale.ROOT).contains(recherche.trim().toLowerCase(Locale.ROOT)));
    }

    private static boolean vide(String s) {
        return s == null || s.isBlank();
    }
}
