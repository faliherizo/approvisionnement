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
import java.util.Set;
import java.util.stream.Collectors;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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
import qc.levis.contrat.clm.domain.PrioriteTrousse;
import qc.levis.contrat.clm.domain.Relances;
import qc.levis.contrat.clm.domain.StatutTrousse;
import qc.levis.contrat.clm.domain.Trousse;

/** Pilotage (ST-13, UI-01, UI-18) : tableau de bord et suivi multi-trousses limité au périmètre de l'acteur. */
@Service
public class SuiviService {

    public static final String BLOCAGE = "BLOCAGE";
    public static final String ERREUR_SALESFORCE = "ERREUR_SALESFORCE";
    public static final String SIGNATURE_EXPIREE = "SIGNATURE_EXPIREE";

    /** {@code priorite} : code BASSE à CRITIQUE ; {@code tri} : « priorite » d'abord les plus prioritaires, sinon les plus récentes. */
    public record Filtre(String q, String statut, String unite, String responsable, String source, LocalDate du, LocalDate au,
                         boolean exceptionsSeulement, String priorite, String tri) {

        /** Ordre des lignes (AF-46) : par priorité décroissante puis récence, ou par récence seule. */
        Sort ordre() {
            return "priorite".equals(tri) ? Sort.by(Sort.Order.desc("prioriteRang")).and(TRI) : TRI;
        }
    }

    private final TrousseRepository trousses;
    private final EtapeTrousseRepository etapes;
    private final EvenementTrousseRepository evenements;
    private final InvitationRepository invitations;
    private final MessageSortantRepository sorties;
    private final ParcoursService parcours;
    private final LectureTrousse lecture;
    private final ContexteClm ctx;
    private final LecturePaginee pagination;

    public SuiviService(TrousseRepository trousses, EtapeTrousseRepository etapes, EvenementTrousseRepository evenements,
                        InvitationRepository invitations, MessageSortantRepository sorties, ParcoursService parcours,
                        LectureTrousse lecture, ContexteClm ctx, LecturePaginee pagination) {
        this.pagination = pagination;
        this.trousses = trousses;
        this.etapes = etapes;
        this.evenements = evenements;
        this.invitations = invitations;
        this.sorties = sorties;
        this.parcours = parcours;
        this.lecture = lecture;
        this.ctx = ctx;
    }

    /** Lignes par page au plus (API et écran) ; l'export CSV en prend davantage. */
    public static final int TAILLE_MAX = 200;
    public static final int TAILLE_DEFAUT = 50;
    /** Lignes exportées au plus en CSV. */
    public static final int LIMITE_EXPORT = 10_000;
    private static final int TRANCHE = 200;
    private static final Sort TRI = Sort.by(Sort.Order.desc("majLe"), Sort.Order.desc("id"));

    /**
     * Une page du suivi (UI-18), les trousses les plus récemment modifiées d'abord. Les filtres s'appliquent en base ; les
     * lignes d'une page sont calculées en quatre requêtes groupées, quel que soit le nombre de trousses de l'organisation
     * (65 000 par an chez IQ ; addenda 3, Q38). Administration et audit voient tout : la page vient directement de la base,
     * avec le total. Pour les autres, la visibilité dépend des affectations et se vérifie trousse par trousse.
     */
    public ClmDto.SuiviPage page(Habilitations.Acteur acteur, Filtre f, int page, int taille) {
        int n = Math.max(1, Math.min(taille, TAILLE_MAX));
        int p = Math.max(0, page);
        Specification<Trousse> spec = specification(ctx.tenant(), f);
        if ((acteur.estAdmin() || acteur.estAuditeur()) && !f.exceptionsSeulement()) {
            Page<Trousse> r = trousses.findAll(spec, PageRequest.of(p, n, f.ordre()));
            return new ClmDto.SuiviPage(lignes(r.getContent()), p, n, r.getTotalElements(), r.hasNext());
        }
        List<ClmDto.SuiviLigne> fenetre = parcourir(acteur, f, spec, p * n, n + 1);
        boolean suivant = fenetre.size() > n;
        return new ClmDto.SuiviPage(suivant ? fenetre.subList(0, n) : fenetre, p, n, null, suivant);
    }

    /** Parcourt les trousses retenues par tranches : saute les {@code ignorer} premières lignes, en garde {@code garder}. */
    private List<ClmDto.SuiviLigne> parcourir(Habilitations.Acteur acteur, Filtre f, Specification<Trousse> spec, int ignorer, int garder) {
        List<ClmDto.SuiviLigne> res = new ArrayList<>();
        int[] aIgnorer = {ignorer};
        Specification<Trousse> candidates = LecturePaginee.voitTout(acteur) ? spec : spec.and(PerimetreVisible.candidates(acteur));
        for (int k = 0; ; k++) {
            Page<Trousse> tranche = trousses.findAll(candidates, PageRequest.of(k, TRANCHE, f.ordre()));
            List<Trousse> visibles = tranche.getContent().stream().filter(t -> lecture.visible(t, acteur)).toList();
            if (f.exceptionsSeulement()) {
                retenir(lignes(visibles).stream().filter(l -> !l.exceptions().isEmpty()).toList(), aIgnorer, garder, res);
            } else {
                int saut = Math.min(aIgnorer[0], visibles.size());
                aIgnorer[0] -= saut;
                List<Trousse> reste = visibles.subList(saut, visibles.size());
                res.addAll(lignes(reste.subList(0, Math.min(reste.size(), garder - res.size()))));
            }
            if (res.size() >= garder || !tranche.hasNext()) {
                return res;
            }
        }
    }

    private static void retenir(List<ClmDto.SuiviLigne> candidates, int[] aIgnorer, int garder, List<ClmDto.SuiviLigne> res) {
        for (ClmDto.SuiviLigne l : candidates) {
            if (aIgnorer[0] > 0) {
                aIgnorer[0]--;
            } else if (res.size() < garder) {
                res.add(l);
            }
        }
    }

    /** Filtres du suivi traduits en critères : l'organisation, puis chaque filtre renseigné. Un code inconnu ne retient rien. */
    static Specification<Trousse> specification(Long tenant, Filtre f) {
        return (r, q, cb) -> {
            List<Predicate> p = new ArrayList<>();
            p.add(cb.equal(r.get("tenantId"), tenant));
            if (!vide(f.statut())) {
                p.add(enumere(StatutTrousse.class, f.statut()).map(v -> cb.equal(r.get("statut"), v)).orElseGet(cb::disjunction));
            }
            if (!vide(f.source())) {
                p.add(enumere(Trousse.Source.class, f.source()).map(v -> cb.equal(r.get("source"), v)).orElseGet(cb::disjunction));
            }
            if (!vide(f.priorite())) {
                p.add(enumere(PrioriteTrousse.class, f.priorite()).map(v -> cb.equal(r.get("priorite"), v)).orElseGet(cb::disjunction));
            }
            if (!vide(f.unite())) {
                p.add(cb.equal(r.get("unite"), f.unite()));
            }
            if (!vide(f.responsable())) {
                p.add(cb.equal(cb.lower(r.get("proprietaire")), f.responsable().trim().toLowerCase(Locale.ROOT)));
            }
            if (f.du() != null) {
                p.add(cb.greaterThanOrEqualTo(r.get("majLe"), f.du().atStartOfDay(ZoneOffset.UTC).toInstant()));
            }
            if (f.au() != null) {
                p.add(cb.lessThan(r.get("majLe"), f.au().plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant()));
            }
            if (!vide(f.q())) {
                String motif = "%" + f.q().trim().toLowerCase(Locale.ROOT).replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
                p.add(cb.or(cb.like(cb.lower(r.get("reference")), motif, '\\'), cb.like(cb.lower(r.get("titre")), motif, '\\'),
                        cb.like(cb.lower(cb.coalesce(r.get("sfDossierId"), "")), motif, '\\')));
            }
            return cb.and(p.toArray(Predicate[]::new));
        };
    }

    private static <E extends Enum<E>> java.util.Optional<E> enumere(Class<E> type, String code) {
        try {
            return java.util.Optional.of(Enum.valueOf(type, code.trim()));
        } catch (IllegalArgumentException e) {
            return java.util.Optional.empty();
        }
    }

    /** Lignes d'un lot de trousses : étapes actives, envois en échec, invitations et dernier événement en une requête chacun. */
    List<ClmDto.SuiviLigne> lignes(List<Trousse> lot) {
        if (lot.isEmpty()) {
            return List.of();
        }
        Instant maintenant = ctx.maintenant();
        Long tenant = ctx.tenant();
        List<Long> ids = lot.stream().map(Trousse::getId).toList();
        Map<Long, List<EtapeTrousse>> actives = etapes.findByTenantIdAndTrousseIdInAndStatut(tenant, ids, Parcours.StatutEtape.ACTIVE).stream()
                .sorted(Comparator.comparingInt(EtapeTrousse::getPhase).thenComparing(EtapeTrousse::getId))
                .collect(Collectors.groupingBy(EtapeTrousse::getTrousseId));
        Set<Long> enEchec = sorties.findByTenantIdAndTrousseIdInAndStatut(tenant, ids, MessageSortant.Statut.ECHEC).stream()
                .map(MessageSortant::getTrousseId).collect(Collectors.toSet());
        Map<Long, List<Invitation>> invitationsParTrousse = invitations.findByTenantIdAndTrousseIdIn(tenant, ids).stream()
                .collect(Collectors.groupingBy(Invitation::getTrousseId));
        Map<Long, EvenementTrousse> derniers = evenements.derniersParTenantIdEtTrousses(tenant, ids).stream()
                .collect(Collectors.toMap(EvenementTrousse::getTrousseId, e -> e, (a, b) -> a));
        return lot.stream().map(t -> ligne(t, actives.getOrDefault(t.getId(), List.of()), enEchec.contains(t.getId()),
                invitationsParTrousse.getOrDefault(t.getId(), List.of()), derniers.get(t.getId()), maintenant)).toList();
    }

    private ClmDto.SuiviLigne ligne(Trousse t, List<EtapeTrousse> actives, boolean echecSalesforce, List<Invitation> invs,
                                    EvenementTrousse dernier, Instant maintenant) {
        List<String> attente = new ArrayList<>();
        actives.forEach(e -> ctx.affectations(e.getAffectationsJson()).forEach(a -> attente.add(a.type().name().toLowerCase() + ":" + a.valeur())));
        Instant echeance = actives.stream().map(EtapeTrousse::getEcheance).filter(java.util.Objects::nonNull).min(Comparator.naturalOrder()).orElse(null);
        List<String> exceptions = new ArrayList<>();
        if (actives.stream().anyMatch(e -> Relances.enRetard(e.getEcheance(), maintenant))) {
            exceptions.add(BLOCAGE);
        }
        if (echecSalesforce) {
            exceptions.add(ERREUR_SALESFORCE);
        }
        if (invs.stream().anyMatch(i -> (i.getStatut() == Invitation.Statut.ENVOYEE || i.getStatut() == Invitation.Statut.VERIFIEE)
                && i.estExpiree(maintenant))) {
            exceptions.add(SIGNATURE_EXPIREE);
        }
        return new ClmDto.SuiviLigne(t.getId(), t.getReference(), t.getTitre(), t.getStatut().name(), t.getSource().name(), t.getUnite(),
                t.getProprietaire(), actives.stream().map(EtapeTrousse::getLibelle).toList(), attente, echeance, exceptions.contains(BLOCAGE),
                dernier == null ? null : dernier.getType(), dernier == null ? null : dernier.getHorodatage(), exceptions,
                t.getPriorite().name());
    }

    /** Export CSV (UI-18) ; les cellules commençant par un caractère de formule sont neutralisées. */
    public String exporterCsv(Habilitations.Acteur acteur, Filtre f) {
        StringBuilder sb = new StringBuilder("reference;titre;priorite;statut;source;unite;responsable;etapes_actives;echeance;exceptions\n");
        for (ClmDto.SuiviLigne l : parcourir(acteur, f, specification(ctx.tenant(), f), 0, LIMITE_EXPORT)) {
            sb.append(String.join(";", cellule(l.reference()), cellule(l.titre()), l.priorite(), l.statut(), l.source(), cellule(l.unite()),
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

    /**
     * Tableau de bord (UI-01) à la volumétrie d'IQ (RST-40) : décomptes en base pour qui voit tout, parcours par tranches
     * sinon ; étapes actives des trousses en cours par lots ; activité et alertes lues au plus récent puis filtrées.
     */
    public ClmDto.TableauBord tableauBord(Habilitations.Acteur acteur, Filtre f) {
        Instant maintenant = ctx.maintenant();
        Long tenant = ctx.tenant();
        Specification<Trousse> spec = specification(tenant, f);
        long[] compte = new long[5];
        if (LecturePaginee.voitTout(acteur)) {
            compte[0] = trousses.count(spec.and(avecStatut(StatutTrousse.BROUILLON)));
            compte[4] = trousses.count(spec.and(avecStatut(StatutTrousse.TERMINEE)));
            pagination.parcourir(acteur, spec.and(avecStatut(StatutTrousse.EN_TRAITEMENT)), lot -> compterEnCours(lot, compte, maintenant));
        } else {
            pagination.parcourir(acteur, spec, lot -> {
                compte[0] += lot.stream().filter(t -> t.getStatut() == StatutTrousse.BROUILLON).count();
                compte[4] += lot.stream().filter(t -> t.getStatut() == StatutTrousse.TERMINEE).count();
                compterEnCours(lot.stream().filter(t -> t.getStatut() == StatutTrousse.EN_TRAITEMENT).toList(), compte, maintenant);
            });
        }
        Map<String, Long> ind = new LinkedHashMap<>();
        ind.put("brouillons", compte[0]);
        ind.put("enValidation", compte[1]);
        ind.put("enSignature", compte[2]);
        ind.put("enRetard", compte[3]);
        ind.put("terminees", compte[4]);
        List<EvenementTrousse> recents = evenements.findTop30ByTenantIdOrderByHorodatageDesc(tenant);
        List<MessageSortant> echecs = sorties.findTop50ByTenantIdAndStatutOrderByCreeLeDesc(tenant, MessageSortant.Statut.ECHEC);
        Set<Long> visibles = visibles(acteur, tenant, recents.stream().map(EvenementTrousse::getTrousseId).collect(Collectors.toSet()),
                echecs.stream().map(MessageSortant::getTrousseId).collect(Collectors.toSet()));
        List<ClmDto.EvenementVue> activite = recents.stream().filter(e -> visibles.contains(e.getTrousseId())).limit(10)
                .map(LectureTrousse::evenement).toList();
        List<ClmDto.SortieVue> alertes = echecs.stream().filter(m -> visibles.contains(m.getTrousseId())).map(SalesforceService::vue).toList();
        return new ClmDto.TableauBord(ind, parcours.mesTaches(acteur, "a_traiter"), activite, alertes);
    }

    private static Specification<Trousse> avecStatut(StatutTrousse statut) {
        return (r, q, cb) -> cb.equal(r.get("statut"), statut);
    }

    /** Trousses en cours d'un lot : en validation, en signature, en retard, d'après leurs étapes actives (une requête). */
    private void compterEnCours(List<Trousse> enCours, long[] compte, Instant maintenant) {
        if (enCours.isEmpty()) {
            return;
        }
        Map<Long, List<EtapeTrousse>> actives = etapes.findByTenantIdAndTrousseIdInAndStatut(ctx.tenant(),
                enCours.stream().map(Trousse::getId).toList(), Parcours.StatutEtape.ACTIVE).stream()
                .collect(Collectors.groupingBy(EtapeTrousse::getTrousseId));
        for (List<EtapeTrousse> lesActives : actives.values()) {
            compte[1] += lesActives.stream().anyMatch(e -> e.getType() != Parcours.TypeEtape.SIGNATURE) ? 1 : 0;
            compte[2] += lesActives.stream().anyMatch(e -> e.getType() == Parcours.TypeEtape.SIGNATURE) ? 1 : 0;
            compte[3] += lesActives.stream().anyMatch(e -> Relances.enRetard(e.getEcheance(), maintenant)) ? 1 : 0;
        }
    }

    /** Parmi les trousses citées par l'activité récente et les alertes, celles que l'acteur voit. */
    private Set<Long> visibles(Habilitations.Acteur acteur, Long tenant, Set<Long> activite, Set<Long> alertes) {
        Set<Long> ids = new java.util.HashSet<>(activite);
        ids.addAll(alertes);
        if (ids.isEmpty()) {
            return Set.of();
        }
        return trousses.findByTenantIdAndIdIn(tenant, ids).stream().filter(t -> lecture.visible(t, acteur))
                .map(Trousse::getId).collect(Collectors.toSet());
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
        List<EvenementTrousse> periode = evenements.findByTenantIdAndHorodatageBetweenOrderByHorodatageDesc(ctx.tenant(),
                du.atStartOfDay().toInstant(ZoneOffset.UTC), au.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC));
        // Références des seules trousses présentes dans la période, et non de toutes celles de l'organisation.
        Map<Long, String> references = new java.util.HashMap<>();
        trousses.findByTenantIdAndIdIn(ctx.tenant(), periode.stream().map(EvenementTrousse::getTrousseId).collect(Collectors.toSet()))
                .forEach(t -> references.put(t.getId(), t.getReference()));
        List<ClmDto.JournalLigne> res = new ArrayList<>();
        for (EvenementTrousse e : periode) {
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
