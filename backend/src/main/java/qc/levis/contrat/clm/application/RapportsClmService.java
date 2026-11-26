package qc.levis.contrat.clm.application;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.clm.application.dto.ClmDto;
import qc.levis.contrat.clm.application.port.ClasseurTableur;
import qc.levis.contrat.clm.application.repository.DepotM365Repository;
import qc.levis.contrat.clm.application.repository.EtapeTrousseRepository;
import qc.levis.contrat.clm.application.repository.EvenementTrousseRepository;
import qc.levis.contrat.clm.application.repository.MessageSortantRepository;
import qc.levis.contrat.clm.application.repository.RappelSortantRepository;
import qc.levis.contrat.clm.application.repository.RapportClmRepository;
import qc.levis.contrat.clm.application.repository.TrousseRepository;
import qc.levis.contrat.clm.domain.DepotM365;
import qc.levis.contrat.clm.domain.EtapeTrousse;
import qc.levis.contrat.clm.domain.EvenementTrousse;
import qc.levis.contrat.clm.domain.MessageSortant;
import qc.levis.contrat.clm.domain.Parcours;
import qc.levis.contrat.clm.domain.RappelSortant;
import qc.levis.contrat.clm.domain.RapportClm;
import qc.levis.contrat.clm.domain.StatutTrousse;
import qc.levis.contrat.clm.domain.Trousse;

/**
 * Rapports personnalisables et exportables (Annexe 10, AF-37 ; ADR-032) : l'administration enregistre des rapports (type,
 * période glissante, filtres, colonnes), les consulte et les exporte en CSV ou en Excel. Les chiffres viennent du journal
 * scellé des trousses et des files d'intégration, jamais d'un compteur tenu à part.
 */
@Service
public class RapportsClmService {

    static final DateTimeFormatter HORODATAGE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /** En-têtes des exports : la langue de travail de l'organisation (Charte de la langue française). */
    static final Map<String, String> ENTETES = Map.ofEntries(Map.entry("reference", "Référence"), Map.entry("titre", "Titre"),
            Map.entry("unite", "Unité"), Map.entry("typeContrat", "Type de contrat"), Map.entry("etape", "Étape"),
            Map.entry("enAttenteDepuis", "En attente depuis"), Map.entry("echeance", "Échéance"), Map.entry("joursAttente", "Jours d'attente"),
            Map.entry("lanceeLe", "Lancée le"), Map.entry("termineeLe", "Terminée le"), Map.entry("delaiHeures", "Délai (h)"),
            Map.entry("nombre", "Nombre"), Map.entry("delaiMoyenHeures", "Délai moyen (h)"), Map.entry("rejeteeLe", "Rejetée le"),
            Map.entry("motif", "Motif"), Map.entry("depasseeLe", "Dépassée le"), Map.entry("indicateur", "Indicateur"),
            Map.entry("valeur", "Valeur"), Map.entry("mois", "Mois"), Map.entry("creees", "Créées"), Map.entry("lancees", "Lancées"),
            Map.entry("terminees", "Terminées"), Map.entry("rejetees", "Rejetées"), Map.entry("date", "Date"),
            Map.entry("categorie", "Catégorie"), Map.entry("detail", "Détail"));

    private final RapportClmRepository rapports;
    private final TrousseRepository trousses;
    private final EvenementTrousseRepository evenements;
    private final EtapeTrousseRepository etapes;
    private final DepotM365Repository depots;
    private final MessageSortantRepository sorties;
    private final RappelSortantRepository rappels;
    private final UtilisationService utilisation;
    private final ClasseurTableur classeur;
    private final JournalClm journal;
    private final ContexteClm ctx;

    public RapportsClmService(RapportClmRepository rapports, TrousseRepository trousses, EvenementTrousseRepository evenements,
                              EtapeTrousseRepository etapes, DepotM365Repository depots, MessageSortantRepository sorties,
                              RappelSortantRepository rappels, UtilisationService utilisation, ClasseurTableur classeur, JournalClm journal,
                              ContexteClm ctx) {
        this.rapports = rapports;
        this.trousses = trousses;
        this.evenements = evenements;
        this.etapes = etapes;
        this.depots = depots;
        this.sorties = sorties;
        this.rappels = rappels;
        this.utilisation = utilisation;
        this.classeur = classeur;
        this.journal = journal;
        this.ctx = ctx;
    }

    // --- Définitions ---------------------------------------------------------------------------------------------------

    public List<ClmDto.TypeRapportVue> types() {
        return java.util.Arrays.stream(RapportClm.Type.values()).map(t -> new ClmDto.TypeRapportVue(t.name(), t.colonnes())).toList();
    }

    public List<ClmDto.RapportVue> lister() {
        return rapports.findByTenantIdOrderByNomAsc(ctx.tenant()).stream().map(RapportsClmService::vue).toList();
    }

    @Transactional
    public ClmDto.RapportVue creer(ClmDto.RapportRequest req, String acteur) {
        RapportClm r = new RapportClm(ctx.tenant(), acteur);
        return enregistrer(r, req, acteur, "créé");
    }

    @Transactional
    public ClmDto.RapportVue modifier(Long id, ClmDto.RapportRequest req, String acteur) {
        return enregistrer(exiger(id), req, acteur, "modifié");
    }

    @Transactional
    public void supprimer(Long id, String acteur) {
        RapportClm r = exiger(id);
        rapports.delete(r);
        journal.configuration(acteur, "RAPPORT", "supprimé : " + r.definition().nom());
    }

    private ClmDto.RapportVue enregistrer(RapportClm r, ClmDto.RapportRequest req, String acteur, String verbe) {
        RapportClm.Definition d = new RapportClm.Definition(req.nom(), type(req.type()), req.periodeJours() == null ? 30 : req.periodeJours(),
                req.unite(), req.typeContrat(), req.statut(), req.colonnes());
        List<String> erreurs = d.erreurs();
        if (!erreurs.isEmpty()) {
            throw new IllegalArgumentException(String.join(" ", erreurs));
        }
        r.definir(d, ctx.maintenant());
        RapportClm enregistre = rapports.save(r);
        journal.configuration(acteur, "RAPPORT", verbe + " : " + d.nom() + " (" + d.type() + ", " + d.periodeJours() + " j)");
        return vue(enregistre);
    }

    private static RapportClm.Type type(String saisi) {
        return java.util.Arrays.stream(RapportClm.Type.values()).filter(t -> t.name().equals(saisi)).findFirst().orElse(null);
    }

    private RapportClm exiger(Long id) {
        return rapports.findByIdAndTenantId(id, ctx.tenant()).orElseThrow(() -> new NoSuchElementException("Rapport introuvable."));
    }

    private static ClmDto.RapportVue vue(RapportClm r) {
        RapportClm.Definition d = r.definition();
        return new ClmDto.RapportVue(r.getId(), d.nom(), d.type().name(), d.periodeJours(), d.unite(), d.typeContrat(), d.statut(),
                d.colonnesRetenues(), r.getProprietaire(), r.getModifieLe());
    }

    // --- Exécution et export -------------------------------------------------------------------------------------------

    public TableauExport executer(Long id) {
        RapportClm.Definition d = exiger(id).definition();
        LocalDate au = ctx.aujourdhui();
        LocalDate du = au.minusDays(d.periodeJours() - 1L);
        List<Map<String, String>> lignes = lignes(d, du, au);
        List<String> colonnes = d.colonnesRetenues();
        return new TableauExport(d.nom() + " — " + du + " au " + au, colonnes.stream().map(ENTETES::get).toList(),
                lignes.stream().map(l -> colonnes.stream().map(c -> l.getOrDefault(c, "")).toList()).toList());
    }

    public byte[] exporter(Long id, String format, String acteur) {
        TableauExport t = executer(id);
        journal.configuration(acteur, "RAPPORT", "exporté (" + format + ") : " + t.titre());
        return fichier(t, format);
    }

    /** Relevé d'utilisation (addenda 3, Q39) dans le même format que les rapports. */
    public byte[] exporterUtilisation(LocalDate du, LocalDate au, String format) {
        List<String> colonnes = RapportClm.Type.UTILISATION.colonnes();
        TableauExport t = new TableauExport("Relevé d'utilisation — " + du + " au " + au, colonnes.stream().map(ENTETES::get).toList(),
                utilisation(du, au).stream().map(l -> colonnes.stream().map(l::get).toList()).toList());
        return fichier(t, format);
    }

    private byte[] fichier(TableauExport t, String format) {
        if ("xlsx".equalsIgnoreCase(format)) {
            return classeur.xlsx("Rapport", t.colonnes(), t.lignes());
        }
        if ("csv".equalsIgnoreCase(format)) {
            return ("﻿" + t.csv()).getBytes(java.nio.charset.StandardCharsets.UTF_8);
        }
        throw new IllegalArgumentException("Format d'export : csv ou xlsx.");
    }

    private List<Map<String, String>> lignes(RapportClm.Definition d, LocalDate du, LocalDate au) {
        return switch (d.type()) {
            case SIGNATURES_EN_ATTENTE -> signaturesEnAttente(d);
            case DELAIS -> delais(d, du, au);
            case DELAIS_ETAPES -> delaisEtapes(d, du, au);
            case COMPLETES -> parEvenement(d, du, au, "TROUSSE_TERMINEE", "termineeLe", null);
            case REJETS -> parEvenement(d, du, au, "TROUSSE_REJETEE", "rejeteeLe", "motif");
            case EXPIRATIONS -> parEvenement(d, du, au, "ECHEANCE_DEPASSEE", "depasseeLe", "etape");
            case UTILISATION -> utilisation(du, au);
            case VOLUMETRIE -> volumetrie(d, du, au);
            case INCIDENTS -> incidents(du, au);
        };
    }

    private List<Map<String, String>> signaturesEnAttente(RapportClm.Definition d) {
        Long tenant = ctx.tenant();
        Map<Long, Trousse> enCours = trousses.findByTenantIdOrderByMajLeDesc(tenant).stream()
                .filter(t -> t.getStatut() == StatutTrousse.EN_TRAITEMENT && retenue(t, d))
                .collect(Collectors.toMap(Trousse::getId, Function.identity(), (a, b) -> a, LinkedHashMap::new));
        Instant maintenant = ctx.maintenant();
        List<Map<String, String>> res = new ArrayList<>();
        for (EtapeTrousse e : etapes.findByTenantIdAndTrousseIdInAndStatut(tenant, enCours.keySet(), Parcours.StatutEtape.ACTIVE)) {
            if (e.getType() != Parcours.TypeEtape.SIGNATURE) {
                continue;
            }
            Map<String, String> l = trousse(enCours.get(e.getTrousseId()));
            l.put("etape", e.getLibelle());
            l.put("enAttenteDepuis", date(e.getActiveeLe()));
            l.put("echeance", date(e.getEcheance()));
            l.put("joursAttente", e.getActiveeLe() == null ? "" : Long.toString(Duration.between(e.getActiveeLe(), maintenant).toDays()));
            res.add(l);
        }
        return res;
    }

    private List<Map<String, String>> delais(RapportClm.Definition d, LocalDate du, LocalDate au) {
        List<Map<String, String>> res = new ArrayList<>();
        List<Duration> tous = new ArrayList<>();
        for (Trousse t : trousses(d, periode(du, au).stream().filter(e -> "TROUSSE_TERMINEE".equals(e.getType())).toList())) {
            List<RapportClm.Evenement> j = journalDe(t);
            RapportClm.delaiTraitement(j).ifPresent(delai -> {
                tous.add(delai);
                Map<String, String> l = trousse(t);
                l.put("lanceeLe", premiere(j, "TROUSSE_LANCEE"));
                l.put("termineeLe", premiere(j, "TROUSSE_TERMINEE"));
                l.put("delaiHeures", Double.toString(RapportClm.heures(delai)));
                res.add(l);
            });
        }
        RapportClm.moyenneHeures(tous).ifPresent(m -> {
            Map<String, String> synthese = new LinkedHashMap<>();
            synthese.put("reference", "Moyenne (" + tous.size() + ")");
            synthese.put("delaiHeures", Double.toString(m));
            res.add(synthese);
        });
        return res;
    }

    private List<Map<String, String>> delaisEtapes(RapportClm.Definition d, LocalDate du, LocalDate au) {
        Map<String, List<Duration>> parEtape = new TreeMap<>();
        Set<String> decisions = Set.of("ETAPE_APPROUVEE", "ETAPE_REJETEE");
        for (Trousse t : trousses(d, periode(du, au).stream().filter(e -> decisions.contains(e.getType())).toList())) {
            RapportClm.delaisEtapes(journalDe(t)).forEach((etape, liste) -> parEtape.computeIfAbsent(etape, k -> new ArrayList<>()).addAll(liste));
        }
        List<Map<String, String>> res = new ArrayList<>();
        parEtape.forEach((etape, liste) -> {
            Map<String, String> l = new LinkedHashMap<>();
            l.put("etape", etape);
            l.put("nombre", Integer.toString(liste.size()));
            l.put("delaiMoyenHeures", RapportClm.moyenneHeures(liste).map(String::valueOf).orElse(""));
            res.add(l);
        });
        return res;
    }

    /** Une ligne par événement de la période (achèvement, rejet, échéance dépassée), pour les trousses retenues. */
    private List<Map<String, String>> parEvenement(RapportClm.Definition d, LocalDate du, LocalDate au, String type, String colonneDate,
                                                   String colonneDetail) {
        List<EvenementTrousse> evts = periode(du, au).stream().filter(e -> type.equals(e.getType())).toList();
        Map<Long, Trousse> parId = trousses(d, evts).stream().collect(Collectors.toMap(Trousse::getId, Function.identity()));
        List<Map<String, String>> res = new ArrayList<>();
        for (EvenementTrousse e : evts) {
            Trousse t = parId.get(e.getTrousseId());
            if (t != null) {
                Map<String, String> l = trousse(t);
                l.put(colonneDate, date(e.getHorodatage()));
                if (colonneDetail != null) {
                    l.put(colonneDetail, Objects.toString(e.getDetail(), ""));
                }
                res.add(l);
            }
        }
        return res;
    }

    private List<Map<String, String>> utilisation(LocalDate du, LocalDate au) {
        ClmDto.UtilisationVue u = utilisation.releve(du, au);
        Map<String, String> valeurs = new LinkedHashMap<>();
        valeurs.put("Enveloppes envoyées", Integer.toString(u.envoyees()));
        valeurs.put("Terminées", Integer.toString(u.terminees()));
        valeurs.put("Rejetées", Integer.toString(u.rejetees()));
        valeurs.put("Annulées après envoi", Integer.toString(u.annuleesApresEnvoi()));
        valeurs.put("Annulées avant envoi", Integer.toString(u.annuleesAvantEnvoi()));
        valeurs.put("Échéances dépassées", Integer.toString(u.echeancesDepassees()));
        valeurs.put("Renvois", Integer.toString(u.renvois()));
        u.parMois().forEach((mois, n) -> valeurs.put("Envoyées en " + mois, Integer.toString(n)));
        return valeurs.entrySet().stream().map(e -> Map.of("indicateur", e.getKey(), "valeur", e.getValue())).toList();
    }

    private List<Map<String, String>> volumetrie(RapportClm.Definition d, LocalDate du, LocalDate au) {
        Map<String, String> colonneDe = Map.of("TROUSSE_CREEE", "creees", "TROUSSE_LANCEE", "lancees", "TROUSSE_TERMINEE", "terminees",
                "TROUSSE_REJETEE", "rejetees");
        List<EvenementTrousse> evts = periode(du, au).stream().filter(e -> colonneDe.containsKey(e.getType())).toList();
        Set<Long> retenues = trousses(d, evts).stream().map(Trousse::getId).collect(Collectors.toSet());
        Map<YearMonth, Map<String, Integer>> parMois = new TreeMap<>();
        for (YearMonth m = YearMonth.from(du); !m.isAfter(YearMonth.from(au)); m = m.plusMonths(1)) {
            parMois.put(m, new LinkedHashMap<>(Map.of("creees", 0, "lancees", 0, "terminees", 0, "rejetees", 0)));
        }
        evts.stream().filter(e -> retenues.contains(e.getTrousseId())).forEach(e -> parMois
                .get(YearMonth.from(e.getHorodatage().atZone(ctx.fuseau()))).merge(colonneDe.get(e.getType()), 1, Integer::sum));
        List<Map<String, String>> res = new ArrayList<>();
        parMois.forEach((mois, n) -> {
            Map<String, String> l = new LinkedHashMap<>();
            l.put("mois", mois.toString());
            n.forEach((k, v) -> l.put(k, Integer.toString(v)));
            res.add(l);
        });
        return res;
    }

    /** Échecs d'intégration, documents infectés, altérations : tout ce qui a exigé ou exige une intervention. */
    private List<Map<String, String>> incidents(LocalDate du, LocalDate au) {
        Instant debut = debut(du);
        Instant fin = fin(au);
        Long tenant = ctx.tenant();
        List<Incident> res = new ArrayList<>();
        List<EvenementTrousse> evts = periode(du, au);
        Map<Long, String> references = references(evts.stream().map(EvenementTrousse::getTrousseId).collect(Collectors.toSet()));
        for (EvenementTrousse e : evts) {
            if ("DOCUMENT_INFECTE".equals(e.getType())) {
                res.add(new Incident(e.getHorodatage(), "ANTIVIRUS", references.get(e.getTrousseId()), e.getDetail()));
            } else if ("INTEGRITE_VERIFIEE".equals(e.getType()) && !"intègre".equals(e.getDetail())) {
                res.add(new Incident(e.getHorodatage(), "INTEGRITE", references.get(e.getTrousseId()), e.getDetail()));
            }
        }
        for (DepotM365 d : depots.findTop100ByTenantIdOrderByCreeLeDesc(tenant)) {
            if (d.getDerniereErreur() != null && dans(d.getCreeLe(), debut, fin)) {
                res.add(new Incident(d.getCreeLe(), "MICROSOFT_365", reference(d.getTrousseId()), d.getNature() + " · " + d.getDerniereErreur()));
            }
        }
        for (MessageSortant m : sorties.findTop100ByTenantIdOrderByCreeLeDesc(tenant)) {
            if (m.getDerniereErreur() != null && dans(m.getCreeLe(), debut, fin)) {
                res.add(new Incident(m.getCreeLe(), "SALESFORCE", reference(m.getTrousseId()), m.getEvenement() + " · " + m.getDerniereErreur()));
            }
        }
        for (RappelSortant r : rappels.findTop100ByTenantIdOrderByCreeLeDesc(tenant)) {
            if (r.getDerniereErreur() != null && dans(r.getCreeLe(), debut, fin)) {
                res.add(new Incident(r.getCreeLe(), "RAPPEL_INTEGRATION", reference(r.getTrousseId()), r.getEvenement() + " · " + r.getDerniereErreur()));
            }
        }
        res.sort(Comparator.comparing(Incident::quand).reversed());
        return res.stream().map(x -> {
            Map<String, String> l = new LinkedHashMap<>();
            l.put("date", date(x.quand()));
            l.put("categorie", x.categorie());
            l.put("reference", Objects.toString(x.reference(), "—"));
            l.put("detail", Objects.toString(x.detail(), ""));
            return l;
        }).toList();
    }

    private record Incident(Instant quand, String categorie, String reference, String detail) {
    }

    // --- Outils --------------------------------------------------------------------------------------------------------

    private List<EvenementTrousse> periode(LocalDate du, LocalDate au) {
        List<EvenementTrousse> evts = new ArrayList<>(evenements.findByTenantIdAndHorodatageBetweenOrderByHorodatageDesc(ctx.tenant(), debut(du), fin(au)));
        java.util.Collections.reverse(evts);
        return evts;
    }

    /** Trousses visées par des événements, une seule fois chacune, filtrées par unité, type de contrat et statut. */
    private List<Trousse> trousses(RapportClm.Definition d, Collection<EvenementTrousse> evts) {
        Set<Long> ids = evts.stream().map(EvenementTrousse::getTrousseId).collect(Collectors.toCollection(java.util.LinkedHashSet::new));
        if (ids.isEmpty()) {
            return List.of();
        }
        return trousses.findByTenantIdAndIdIn(ctx.tenant(), ids).stream().filter(t -> retenue(t, d))
                .sorted(Comparator.comparing(Trousse::getId)).toList();
    }

    static boolean retenue(Trousse t, RapportClm.Definition d) {
        return (d.unite() == null || d.unite().equals(t.getUnite()))
                && (d.typeContrat() == null || d.typeContrat().equals(t.getTypeContrat()))
                && (d.statut() == null || d.statut().equals(t.getStatut().name()));
    }

    private List<RapportClm.Evenement> journalDe(Trousse t) {
        return evenements.findByTenantIdAndTrousseIdOrderBySequenceAsc(t.getTenantId(), t.getId()).stream()
                .map(e -> new RapportClm.Evenement(e.getType(), e.getDetail(), e.getHorodatage())).toList();
    }

    private String premiere(List<RapportClm.Evenement> j, String type) {
        return j.stream().filter(e -> type.equals(e.type())).findFirst().map(e -> date(e.horodatage())).orElse("");
    }

    private Map<String, String> trousse(Trousse t) {
        Map<String, String> l = new LinkedHashMap<>();
        l.put("reference", t.getReference());
        l.put("titre", t.getTitre());
        l.put("unite", Objects.toString(t.getUnite(), ""));
        l.put("typeContrat", Objects.toString(t.getTypeContrat(), ""));
        return l;
    }

    private Map<Long, String> references(Set<Long> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        return trousses.findByTenantIdAndIdIn(ctx.tenant(), ids).stream().collect(Collectors.toMap(Trousse::getId, Trousse::getReference));
    }

    private String reference(Long trousseId) {
        return trousses.findByIdAndTenantId(trousseId, ctx.tenant()).map(Trousse::getReference).orElse(null);
    }

    private String date(Instant i) {
        return i == null ? "" : HORODATAGE.format(i.atZone(ctx.fuseau()));
    }

    private Instant debut(LocalDate du) {
        return du.atStartOfDay(ctx.fuseau()).toInstant();
    }

    private Instant fin(LocalDate au) {
        return au.plusDays(1).atStartOfDay(ctx.fuseau()).toInstant().minusMillis(1);
    }

    private static boolean dans(Instant i, Instant debut, Instant fin) {
        return !i.isBefore(debut) && !i.isAfter(fin);
    }
}
