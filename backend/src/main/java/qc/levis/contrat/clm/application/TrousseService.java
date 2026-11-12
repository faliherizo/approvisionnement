package qc.levis.contrat.clm.application;

import com.fasterxml.jackson.core.type.TypeReference;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.clm.application.dto.ClmDto;
import qc.levis.contrat.clm.application.port.PortSalesforce;
import qc.levis.contrat.clm.application.repository.DocumentTrousseRepository;
import qc.levis.contrat.clm.application.repository.EtapeTrousseRepository;
import qc.levis.contrat.clm.application.repository.EvenementTrousseRepository;
import qc.levis.contrat.clm.application.repository.InvitationRepository;
import qc.levis.contrat.clm.application.repository.ModeleContratRepository;
import qc.levis.contrat.clm.application.repository.ParticipantRepository;
import qc.levis.contrat.clm.application.repository.TrousseRepository;
import qc.levis.contrat.clm.domain.ChaineScellee;
import qc.levis.contrat.clm.domain.DocumentTrousse;
import qc.levis.contrat.clm.domain.EtapeTrousse;
import qc.levis.contrat.clm.domain.Habilitations;
import qc.levis.contrat.clm.domain.Identite;
import qc.levis.contrat.clm.domain.Invitation;
import qc.levis.contrat.clm.domain.Modele;
import qc.levis.contrat.clm.domain.Suggestions;
import qc.levis.contrat.clm.domain.ModeleContrat;
import qc.levis.contrat.clm.domain.ParametresClm;
import qc.levis.contrat.clm.domain.Parcours;
import qc.levis.contrat.clm.domain.Participant;
import qc.levis.contrat.clm.domain.Preremplissage;
import qc.levis.contrat.clm.domain.Relances;
import qc.levis.contrat.clm.domain.StatutTrousse;
import qc.levis.contrat.clm.domain.Trousse;

/** Cycle de vie d'une trousse (ST-03, UI-02 à UI-04, UI-10) : création, paramètres, participants, parcours, lancement. */
@Service
public class TrousseService {

    static final TypeReference<List<Modele.Champ>> CHAMPS = new TypeReference<>() {
    };
    static final TypeReference<List<ClmDto.EtapeRequest>> PARCOURS = new TypeReference<>() {
    };
    private static final TypeReference<Map<String, Preremplissage.Valeur>> VALEURS = new TypeReference<>() {
    };

    private final TrousseRepository trousses;
    private final ParticipantRepository participants;
    private final EtapeTrousseRepository etapes;
    private final DocumentTrousseRepository documents;
    private final InvitationRepository invitations;
    private final EvenementTrousseRepository evenements;
    private final ModeleContratRepository modeles;
    private final ParcoursService parcours;
    private final SalesforceService salesforce;
    private final DiffusionStatut diffusion;
    private final JournalClm journal;
    private final LectureTrousse lecture;
    private final ParametresClmService parametres;
    private final ListesClm listes;
    private final ContexteClm ctx;

    public TrousseService(TrousseRepository trousses, ParticipantRepository participants, EtapeTrousseRepository etapes,
                          DocumentTrousseRepository documents, InvitationRepository invitations, EvenementTrousseRepository evenements,
                          ModeleContratRepository modeles, ParcoursService parcours, SalesforceService salesforce, DiffusionStatut diffusion, JournalClm journal,
                          LectureTrousse lecture, ParametresClmService parametres, ListesClm listes, ContexteClm ctx) {
        this.trousses = trousses;
        this.participants = participants;
        this.etapes = etapes;
        this.documents = documents;
        this.invitations = invitations;
        this.evenements = evenements;
        this.modeles = modeles;
        this.parcours = parcours;
        this.salesforce = salesforce;
        this.diffusion = diffusion;
        this.journal = journal;
        this.lecture = lecture;
        this.parametres = parametres;
        this.listes = listes;
        this.ctx = ctx;
    }

    // --- Création et paramètres (UI-03) ---
    @Transactional
    public ClmDto.TrousseDetail creer(ClmDto.TrousseRequest req, Habilitations.Acteur acteur) {
        Long tenant = ctx.tenant();
        ParametresClm.Reglages reglages = parametres.reglages(tenant);
        String titre = exiger(req.titre(), "Titre requis.");
        String reference = req.reference() == null || req.reference().isBlank()
                ? prochaineReference(tenant, reglages.prefixeReference())
                : req.reference().trim().toUpperCase(Locale.ROOT);
        if (trousses.existsByTenantIdAndReference(tenant, reference)) {
            throw new IllegalStateException("Doublon de référence : " + reference);
        }
        Trousse t = new Trousse(tenant, reference, titre, acteur.id(), ctx.maintenant());
        t.setRappelFrequenceJours(reglages.rappelFrequenceJours());
        t.setRappelsMax(reglages.rappelsMax());
        appliquer(t, req);
        if (req.modeleId() != null) {
            ModeleContrat m = modelePublie(req.modeleId());
            t.setModeleId(m.getId());
            if (t.getTypeContrat() == null) {
                t.setTypeContrat(m.getCategorie());
            }
        }
        t = trousses.save(t);
        journal.consigner(t, "TROUSSE_CREEE", acteur.id(), t.getReference());
        if (t.getModeleId() != null) {
            copierParcoursDuModele(t);
        }
        if (req.sfDossierId() != null && !req.sfDossierId().isBlank()) {
            preremplir(t, req.sfDossierId(), Map.of(), acteur.id());
        }
        return detail(t, acteur);
    }

    @Transactional
    public ClmDto.TrousseDetail modifier(Long id, ClmDto.TrousseRequest req, Habilitations.Acteur acteur) {
        Trousse t = lecture.exigerGestion(id, acteur);
        t.exigerModifiable();
        if (req.titre() != null) {
            t.setTitre(exiger(req.titre(), "Titre requis."));
        }
        appliquer(t, req);
        t.toucher(ctx.maintenant());
        trousses.save(t);
        journal.consigner(t, "TROUSSE_MODIFIEE", acteur.id(), null);
        return detail(t, acteur);
    }

    /** Référence attribuée : préfixe de l'organisation, année et rang (ex. TR-2026-00001). */
    private String prochaineReference(Long tenant, String prefixe) {
        int annee = ctx.maintenant().atZone(java.time.ZoneOffset.UTC).getYear();
        long n = trousses.findByTenantIdOrderByMajLeDesc(tenant).size() + 1L;
        String ref = String.format("%s-%d-%05d", prefixe, annee, n);
        while (trousses.existsByTenantIdAndReference(tenant, ref)) {
            ref = String.format("%s-%d-%05d", prefixe, annee, ++n);
        }
        return ref;
    }

    /** Type et unité viennent des listes de l'organisation ; une valeur inchangée reste admise même retirée depuis. */
    private void appliquer(Trousse t, ClmDto.TrousseRequest req) {
        if (req.typeContrat() != null && !Objects.equals(vide(req.typeContrat()), t.getTypeContrat())) {
            t.setTypeContrat(listes.exiger(ListesClm.Liste.TYPE_CONTRAT, req.typeContrat()));
        }
        if (req.unite() != null && !Objects.equals(vide(req.unite()), t.getUnite())) {
            t.setUnite(listes.exiger(ListesClm.Liste.UNITE, req.unite()));
        }
        if (req.echeance() != null) t.setEcheance(req.echeance());
        if (req.copies() != null) t.setCopies(vide(req.copies()));
        if (req.risque() != null && !req.risque().isBlank()) t.setRisque(Identite.Risque.valueOf(req.risque().trim().toUpperCase()));
        if (req.rappelFrequenceJours() != null) {
            if (req.rappelFrequenceJours() < 1 || req.rappelFrequenceJours() > Relances.FREQUENCE_MAX_JOURS) {
                throw new IllegalArgumentException("Fréquence de rappel entre 1 et " + Relances.FREQUENCE_MAX_JOURS + " jours.");
            }
            t.setRappelFrequenceJours(req.rappelFrequenceJours());
        }
        if (req.rappelsMax() != null) {
            if (req.rappelsMax() < 0 || req.rappelsMax() > Relances.RAPPELS_PLAFOND) {
                throw new IllegalArgumentException("Nombre de rappels entre 0 et " + Relances.RAPPELS_PLAFOND + ".");
            }
            t.setRappelsMax(req.rappelsMax());
        }
    }

    private ModeleContrat modelePublie(Long id) {
        ModeleContrat m = modeles.findByIdAndTenantId(id, ctx.tenant())
                .orElseThrow(() -> new NoSuchElementException("Modèle introuvable : " + id));
        if (m.getStatut() != Modele.Statut.PUBLIE) {
            throw new IllegalStateException("Seul un modèle publié peut servir à une trousse.");
        }
        return m;
    }

    /** Le modèle désigne ses signataires par rôle (ex. SIGNATAIRE_1) ; ils sont résolus à l'ajout des participants. */
    private void copierParcoursDuModele(Trousse t) {
        modeles.findByIdAndTenantId(t.getModeleId(), t.getTenantId()).ifPresent(m -> {
            List<ClmDto.EtapeRequest> defs = ctx.lire(m.getParcoursJson(), PARCOURS, List.of());
            if (!defs.isEmpty()) {
                enregistrerEtapes(t, definitions(defs));
                journal.consigner(t, "PARCOURS_MODELE", "systeme", m.getNom() + " v" + m.getNumero());
            }
        });
    }

    private void resoudreRole(Trousse t, Participant p) {
        for (EtapeTrousse e : parcours.etapesDe(t)) {
            List<Parcours.Affectation> affs = ctx.affectations(e.getAffectationsJson());
            List<Parcours.Affectation> resolues = affs.stream()
                    .map(a -> a.type() == Parcours.TypeAffecte.PARTICIPANT && a.valeur().equalsIgnoreCase(p.getRoleContrat())
                            ? new Parcours.Affectation(Parcours.TypeAffecte.PARTICIPANT, String.valueOf(p.getId())) : a)
                    .toList();
            if (!resolues.equals(affs)) {
                e.setAffectationsJson(ctx.ecrire(resolues));
                etapes.save(e);
            }
        }
    }

    // --- Préremplissage Salesforce (UI-10) ---
    @Transactional
    public ClmDto.PreremplissageVue preremplir(Long id, ClmDto.PreremplissageRequest req, Habilitations.Acteur acteur) {
        Trousse t = lecture.exigerGestion(id, acteur);
        t.exigerOuverte();
        String sfId = req.sfDossierId() == null || req.sfDossierId().isBlank() ? t.getSfDossierId() : req.sfDossierId();
        return preremplir(t, sfId, req.valeursManuelles() == null ? Map.of() : req.valeursManuelles(), acteur.id());
    }

    private ClmDto.PreremplissageVue preremplir(Trousse t, String sfId, Map<String, Object> manuelles, String acteur) {
        PortSalesforce.Dossier dossier = salesforce.lireDossier(sfId);
        List<Modele.Champ> champs = champsDuModele(t);
        Preremplissage.Resultat r = Preremplissage.appliquer(champs, dossier.champs(), dossier.recupereLe(), manuelles);
        t.setSource(Trousse.Source.SALESFORCE);
        t.setSfDossierId(dossier.id());
        t.setSfRecupereLe(dossier.recupereLe());
        t.setValeursJson(ctx.ecrire(r.valeurs()));
        t.toucher(ctx.maintenant());
        trousses.save(t);
        journal.consigner(t, "PREREMPLISSAGE_SALESFORCE", acteur, dossier.numero() + " · " + r.valeurs().size() + " valeur(s), "
                + r.manquants().size() + " manquant(s), " + r.conflits().size() + " conflit(s)");
        return vuePreremplissage(dossier, champs, r);
    }

    static ClmDto.PreremplissageVue vuePreremplissage(PortSalesforce.Dossier dossier, List<Modele.Champ> champs, Preremplissage.Resultat r) {
        return new ClmDto.PreremplissageVue(dossier.id(), dossier.numero(), dossier.champs(), dossier.recupereLe(),
                correspondances(champs, r), r.manquants(), r.conflits());
    }

    private static List<ClmDto.Correspondance> correspondances(List<Modele.Champ> champs, Preremplissage.Resultat r) {
        List<ClmDto.Correspondance> corr = new ArrayList<>();
        for (Modele.Champ c : champs) {
            if (!c.completeALaGeneration()) {
                continue;
            }
            Preremplissage.Valeur v = r.valeurs().get(c.cle());
            corr.add(new ClmDto.Correspondance(c.cle(), c.libelle(), c.source(), v == null ? null : v.valeur(),
                    v == null ? null : v.provenance().name(), v == null ? null : v.recupereLe()));
        }
        return corr;
    }

    /**
     * Saisie ou correction des valeurs que l'initiateur complète (F-02, F-04) : chaque valeur est contrôlée contre son
     * champ, une valeur vide retire la saisie, et une trousse Salesforce est relue pour que l'écart reste tracé en conflit.
     */
    @Transactional
    public ClmDto.PreremplissageVue saisirValeurs(Long id, ClmDto.ValeursRequest req, Habilitations.Acteur acteur) {
        Trousse t = lecture.exigerGestion(id, acteur);
        t.exigerOuverte();
        List<Modele.Champ> champs = champsDuModele(t);
        if (champs.isEmpty()) {
            throw new IllegalStateException("Aucun modèle associé : la trousse n'a pas de champ à compléter.");
        }
        Map<String, Object> manuelles = new LinkedHashMap<>();
        valeurs(t).forEach((cle, v) -> {
            if (v.provenance() == Preremplissage.Provenance.MANUEL) {
                manuelles.put(cle, v.valeur());
            }
        });
        List<String> erreurs = appliquerSaisie(champs, req.valeurs() == null ? Map.of() : req.valeurs(), manuelles);
        if (!erreurs.isEmpty()) {
            throw new IllegalArgumentException(String.join(" ", erreurs));
        }
        if (t.getSource() == Trousse.Source.SALESFORCE) {
            return preremplir(t, t.getSfDossierId(), manuelles, acteur.id());
        }
        Preremplissage.Resultat r = Preremplissage.appliquer(champs, Map.of(), null, manuelles);
        t.setValeursJson(ctx.ecrire(r.valeurs()));
        t.toucher(ctx.maintenant());
        trousses.save(t);
        journal.consigner(t, "VALEURS_SAISIES", acteur.id(), r.valeurs().size() + " valeur(s), " + r.manquants().size() + " manquant(s)");
        return new ClmDto.PreremplissageVue(null, null, Map.of(), null, correspondances(champs, r), r.manquants(), r.conflits());
    }

    private static List<String> appliquerSaisie(List<Modele.Champ> champs, Map<String, Object> saisie, Map<String, Object> manuelles) {
        Map<String, Modele.Champ> parCle = new LinkedHashMap<>();
        champs.stream().filter(Modele.Champ::completeALaGeneration).forEach(c -> parCle.put(c.cle(), c));
        List<String> erreurs = new ArrayList<>();
        saisie.forEach((cle, brute) -> {
            Modele.Champ c = parCle.get(cle);
            String valeur = brute == null ? "" : String.valueOf(brute).trim();
            if (c == null) {
                erreurs.add("Champ inconnu ou non modifiable : " + cle + ".");
            } else if (valeur.isEmpty()) {
                manuelles.remove(cle);
            } else if (Modele.valeurValide(c, valeur)) {
                manuelles.put(cle, valeur);
            } else {
                erreurs.add("Valeur invalide pour « " + c.libelle() + " ».");
            }
        });
        return erreurs;
    }

    List<Modele.Champ> champsDuModele(Trousse t) {
        if (t.getModeleId() == null) {
            return List.of();
        }
        return modeles.findByIdAndTenantId(t.getModeleId(), t.getTenantId())
                .map(m -> ctx.lire(m.getChampsJson(), CHAMPS, List.<Modele.Champ>of())).orElse(List.of());
    }

    /**
     * Saisie assistée d'un champ texte (F-04) : valeurs que l'acteur a déjà retenues pour ce champ dans ses propres
     * trousses. Les trousses d'autrui ne sont jamais lues, afin qu'aucune donnée hors périmètre ne soit révélée.
     */
    public List<String> suggestions(Long id, String cle, Habilitations.Acteur acteur) {
        Trousse t = lecture.exigerGestion(id, acteur);
        Modele.Champ champ = champsDuModele(t).stream().filter(c -> c.cle().equals(cle)).findFirst()
                .orElseThrow(() -> new java.util.NoSuchElementException("Champ inconnu dans le modèle de la trousse : " + cle));
        if (champ.type() != Modele.TypeChamp.TEXTE) {
            return List.of();
        }
        Preremplissage.Valeur actuelle = valeurs(t).get(cle);
        List<Map<String, Preremplissage.Valeur>> jeux = trousses
                .findTop200ByTenantIdAndProprietaireIgnoreCaseOrderByMajLeDesc(t.getTenantId(), acteur.id()).stream()
                .filter(autre -> !autre.getId().equals(t.getId())).map(this::valeurs).toList();
        return Suggestions.pour(cle, jeux, actuelle == null || actuelle.valeur() == null ? null : String.valueOf(actuelle.valeur()));
    }

    Map<String, Preremplissage.Valeur> valeurs(Trousse t) {
        return ctx.lire(t.getValeursJson(), VALEURS, Map.of());
    }

    // --- Participants (UI-15) ---
    @Transactional
    public ClmDto.ParticipantVue ajouterParticipant(Long id, ClmDto.ParticipantRequest req, Habilitations.Acteur acteur) {
        Trousse t = lecture.exigerGestion(id, acteur);
        t.exigerOuverte();
        String nom = exiger(req.nom(), "Nom du participant requis.");
        String courriel = req.courriel() == null ? null : req.courriel().trim().toLowerCase(Locale.ROOT);
        String role = listes.exiger(ListesClm.Liste.ROLE_CONTRAT, exiger(req.roleContrat(), "Rôle dans le contrat requis."))
                .toUpperCase(Locale.ROOT);
        Identite.Niveau demande = req.niveauIdentite() == null || req.niveauIdentite().isBlank() ? null
                : Identite.Niveau.valueOf(req.niveauIdentite().trim().toUpperCase());
        Identite.Niveau niveau = parametres.reglages(t.getTenantId()).identite().niveauApplique(t.getRisque(), req.externe(), demande);
        Identite.verifierCoordonnees(niveau, courriel, req.telephone(), req.question(), req.reponse());
        boolean doublon = participants.findByTenantIdAndTrousseIdOrderByIdAsc(t.getTenantId(), t.getId()).stream()
                .anyMatch(p -> p.getCourriel().equalsIgnoreCase(courriel) || p.getRoleContrat().equals(role));
        if (doublon) {
            throw new IllegalStateException("Signataire en doublon (courriel ou rôle déjà présent).");
        }
        Participant p = new Participant(t.getTenantId(), t.getId(), nom, courriel, vide(req.telephone()), role, req.externe(), niveau);
        if (niveau == Identite.Niveau.QUESTION) {
            p.definirQuestion(req.question().trim(), ChaineScellee.sha256(Identite.normaliserReponse(req.reponse())
                    .getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        }
        p = participants.save(p);
        resoudreRole(t, p);
        journal.consigner(t, "PARTICIPANT_AJOUTE", acteur.id(), nom + " · " + role + " · " + niveau
                + (t.enTraitement() ? " · ajouté en cours de parcours" : ""));
        return LectureTrousse.participant(p);
    }

    @Transactional
    public void retirerParticipant(Long id, Long participantId, Habilitations.Acteur acteur) {
        Trousse t = lecture.exigerGestion(id, acteur);
        t.exigerOuverte();
        Participant p = participants.findByIdAndTenantId(participantId, t.getTenantId())
                .filter(x -> x.getTrousseId().equals(t.getId()))
                .orElseThrow(() -> new NoSuchElementException("Participant introuvable : " + participantId));
        boolean affecte = parcours.etapesDe(t).stream().flatMap(e -> ctx.affectations(e.getAffectationsJson()).stream())
                .anyMatch(a -> a.type() == Parcours.TypeAffecte.PARTICIPANT && a.valeur().equals(String.valueOf(participantId)));
        if (affecte) {
            throw new IllegalStateException("Participant affecté à une étape : retirez-le du parcours d'abord.");
        }
        participants.delete(p);
        journal.consigner(t, "PARTICIPANT_RETIRE", acteur.id(), p.getNom());
    }

    /** Attribuer une zone (UI-15) : le participant prend le rôle de la zone ; celui qui le tenait reçoit l'ancien rôle. */
    @Transactional
    public List<ClmDto.ParticipantVue> attribuerZone(Long id, Long participantId, ClmDto.ZoneRequest req, Habilitations.Acteur acteur) {
        Trousse t = lecture.exigerGestion(id, acteur);
        t.exigerModifiable();
        List<Participant> tous = participants.findByTenantIdAndTrousseIdOrderByIdAsc(t.getTenantId(), t.getId());
        Participant p = tous.stream().filter(x -> x.getId().equals(participantId)).findFirst()
                .orElseThrow(() -> new NoSuchElementException("Participant introuvable : " + participantId));
        String ancien = p.getRoleContrat();
        p.attribuerRole(listes.exiger(ListesClm.Liste.ROLE_CONTRAT, req.roleContrat()));
        for (Participant autre : tous) {
            if (autre != p && autre.getRoleContrat().equals(p.getRoleContrat())) {
                autre.attribuerRole(ancien);
                participants.save(autre);
                resoudreRole(t, autre);
            }
        }
        participants.save(p);
        resoudreRole(t, p);
        journal.consigner(t, "ZONE_ATTRIBUEE", acteur.id(), p.getNom() + " · " + p.getRoleContrat());
        return tous.stream().map(LectureTrousse::participant).toList();
    }

    // --- Parcours (UI-11) ---
    @Transactional
    public List<ClmDto.EtapeVue> definirParcours(Long id, List<ClmDto.EtapeRequest> req, Habilitations.Acteur acteur) {
        Trousse t = lecture.exigerGestion(id, acteur);
        t.exigerModifiable();
        List<Parcours.DefinitionEtape> defs = definitions(req);
        List<String> erreurs = new ArrayList<>(Parcours.valider(defs));
        erreurs.addAll(participantsInconnus(t, defs));
        erreurs.addAll(reglesInconnues(champsDuModele(t), defs));
        if (!erreurs.isEmpty()) {
            throw new IllegalArgumentException(String.join(" ", erreurs));
        }
        enregistrerEtapes(t, defs);
        journal.consigner(t, "PARCOURS_DEFINI", acteur.id(), defs.size() + " étape(s) sur "
                + defs.stream().mapToInt(Parcours.DefinitionEtape::phase).max().orElse(0) + " phase(s)");
        Instant maintenant = ctx.maintenant();
        return parcours.etapesDe(t).stream().map(e -> lecture.etape(e, maintenant)).toList();
    }

    /** « Tester parcours » : validation sans enregistrement, avec un résumé lisible de l'enchaînement. */
    public ClmDto.Validation testerParcours(Long id, List<ClmDto.EtapeRequest> req, Habilitations.Acteur acteur) {
        Trousse t = lecture.exigerVisible(id, acteur);
        List<String> erreurs = new ArrayList<>();
        try {
            List<Parcours.DefinitionEtape> defs = definitions(req);
            erreurs.addAll(Parcours.valider(defs));
            erreurs.addAll(participantsInconnus(t, defs));
            erreurs.addAll(reglesInconnues(champsDuModele(t), defs));
        } catch (IllegalArgumentException e) {
            erreurs.add(e.getMessage());
        }
        return new ClmDto.Validation(erreurs.isEmpty(), erreurs);
    }

    private List<String> participantsInconnus(Trousse t, List<Parcours.DefinitionEtape> defs) {
        Set<String> ids = new HashSet<>();
        participants.findByTenantIdAndTrousseIdOrderByIdAsc(t.getTenantId(), t.getId()).forEach(p -> ids.add(String.valueOf(p.getId())));
        List<String> erreurs = new ArrayList<>();
        for (Parcours.DefinitionEtape d : defs) {
            for (Parcours.Affectation a : d.affectations()) {
                if (a.type() == Parcours.TypeAffecte.PARTICIPANT && !ids.contains(a.valeur())) {
                    erreurs.add("Participant inconnu dans l'étape " + d.cle() + " : " + a.valeur());
                }
            }
        }
        return erreurs;
    }

    /** Une règle d'étape ne porte que sur un champ que l'initiateur complète : lui seul a une valeur au lancement. */
    static List<String> reglesInconnues(List<Modele.Champ> champs, List<Parcours.DefinitionEtape> defs) {
        Set<String> cles = new HashSet<>();
        champs.stream().filter(Modele.Champ::completeALaGeneration).forEach(c -> cles.add(c.cle()));
        List<String> erreurs = new ArrayList<>();
        for (Parcours.DefinitionEtape d : defs) {
            for (Modele.Condition c : d.conditions()) {
                if (c.champ() != null && !c.champ().isBlank() && !cles.contains(c.champ())) {
                    erreurs.add("Règle de l'étape " + d.cle() + " : « " + c.champ() + " » n'est pas un champ que l'initiateur complète.");
                }
            }
        }
        return erreurs;
    }

    private void enregistrerEtapes(Trousse t, List<Parcours.DefinitionEtape> defs) {
        etapes.deleteAll(parcours.etapesDe(t));
        for (Parcours.DefinitionEtape d : defs) {
            etapes.save(new EtapeTrousse(t.getTenantId(), t.getId(), d, ctx.ecrire(d.affectations()), ctx.ecrire(d.conditions())));
        }
    }

    static List<Parcours.DefinitionEtape> definitions(List<ClmDto.EtapeRequest> req) {
        if (req == null) {
            return List.of();
        }
        List<Parcours.DefinitionEtape> res = new ArrayList<>();
        for (ClmDto.EtapeRequest r : req) {
            Parcours.TypeEtape type = enumere(Parcours.TypeEtape.class, r.type(), "Type d'étape invalide : " + r.type());
            List<Parcours.Affectation> affs = affectations(r.affectations());
            int quorum = r.quorum() == null ? (type == Parcours.TypeEtape.SIGNATURE ? affs.size() : 1) : r.quorum();
            Parcours.PolitiqueRejet politique = r.politique() == null || r.politique().isBlank() ? Parcours.PolitiqueRejet.UN_REFUS
                    : enumere(Parcours.PolitiqueRejet.class, r.politique(), "Politique de rejet invalide : " + r.politique());
            res.add(new Parcours.DefinitionEtape(r.cle(), r.libelle(), r.phase(), type, affs, quorum, politique, r.delaiJours(),
                    r.conditions()));
        }
        return res;
    }

    static List<Parcours.Affectation> affectations(List<ClmDto.AffectationDto> dtos) {
        List<Parcours.Affectation> res = new ArrayList<>();
        for (ClmDto.AffectationDto a : dtos == null ? List.<ClmDto.AffectationDto>of() : dtos) {
            res.add(new Parcours.Affectation(enumere(Parcours.TypeAffecte.class, a.type(), "Type d'affectation invalide : " + a.type()), a.valeur()));
        }
        return res;
    }

    private static <E extends Enum<E>> E enumere(Class<E> type, String valeur, String message) {
        try {
            return Enum.valueOf(type, valeur == null ? "" : valeur.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(message);
        }
    }

    // --- Lancement, annulation, reprise ---
    @Transactional
    public ClmDto.TrousseDetail lancer(Long id, Habilitations.Acteur acteur) {
        Trousse t = lecture.exigerGestion(id, acteur);
        t.exigerModifiable();
        List<DocumentTrousse> courants = documentsCourants(t);
        List<String> erreurs = new ArrayList<>();
        if (courants.isEmpty()) {
            erreurs.add("Ajoutez au moins un document.");
        }
        if (courants.stream().anyMatch(d -> d.getAnalyse() != DocumentTrousse.Analyse.PROPRE)) {
            erreurs.add("Analyse antivirus en attente ou en échec sur un document.");
        }
        List<EtapeTrousse> lesEtapes = parcours.etapesDe(t);
        List<Parcours.DefinitionEtape> defs = lesEtapes.stream().map(e -> new Parcours.DefinitionEtape(e.getCle(), e.getLibelle(),
                e.getPhase(), e.getType(), ctx.affectations(e.getAffectationsJson()), e.getQuorum(), e.getPolitique(), e.getDelaiJours(),
                ctx.conditions(e.getConditionsJson()))).toList();
        List<Modele.Champ> champs = champsDuModele(t);
        erreurs.addAll(Parcours.valider(defs));
        erreurs.addAll(participantsInconnus(t, defs));
        erreurs.addAll(reglesInconnues(champs, defs));
        List<String> manquants = Preremplissage.appliquer(champs, Map.of(), null, rawValeurs(t)).manquants();
        if (!manquants.isEmpty()) {
            erreurs.add("Données incomplètes : " + String.join(", ", manquants));
        }
        if (!erreurs.isEmpty()) {
            throw new IllegalArgumentException(String.join(" ", erreurs));
        }
        for (DocumentTrousse d : courants) {
            d.setStatut(DocumentTrousse.Statut.ENVOYE);
            documents.save(d);
        }
        t.changerStatut(StatutTrousse.EN_TRAITEMENT, ctx.maintenant());
        trousses.save(t);
        long seq = journal.consigner(t, "TROUSSE_LANCEE", acteur.id(), courants.size() + " document(s)").getSequence();
        diffusion.enfiler(t, "LANCEE", seq, Map.of());
        parcours.demarrer(t, acteur.id());
        return detail(t, acteur);
    }

    private Map<String, Object> rawValeurs(Trousse t) {
        return ctx.valeursBrutes(t.getValeursJson());
    }

    @Transactional
    public ClmDto.TrousseDetail annuler(Long id, String motif, Habilitations.Acteur acteur) {
        Trousse t = lecture.exigerGestion(id, acteur);
        t.changerStatut(StatutTrousse.ANNULEE, ctx.maintenant());
        trousses.save(t);
        parcours.annulerInvitations(t);
        long seq = journal.consigner(t, "TROUSSE_ANNULEE", acteur.id(), motif).getSequence();
        diffusion.enfiler(t, "ANNULEE", seq, Map.of("motif", motif == null ? "" : motif));
        return detail(t, acteur);
    }

    /** Une trousse rejetée redevient brouillon : documents et parcours sont corrigés puis relancés (nouveau cycle). */
    @Transactional
    public ClmDto.TrousseDetail reprendre(Long id, Habilitations.Acteur acteur) {
        Trousse t = lecture.exigerGestion(id, acteur);
        if (t.getStatut() != StatutTrousse.REJETEE) {
            throw new IllegalStateException("Seule une trousse rejetée peut être reprise.");
        }
        t.changerStatut(StatutTrousse.BROUILLON, ctx.maintenant());
        trousses.save(t);
        for (EtapeTrousse e : parcours.etapesDe(t)) {
            e.reinitialiser();
            etapes.save(e);
        }
        for (DocumentTrousse d : documentsCourants(t)) {
            if (d.getStatut() == DocumentTrousse.Statut.ENVOYE) {
                d.setStatut(DocumentTrousse.Statut.BROUILLON);
                documents.save(d);
            }
        }
        journal.consigner(t, "TROUSSE_REPRISE", acteur.id(), null);
        return detail(t, acteur);
    }

    // --- Lecture (UI-02, UI-04) ---
    public List<ClmDto.TrousseResume> lister(Habilitations.Acteur acteur, ClmDto.CritereListe c) {
        Instant maintenant = ctx.maintenant();
        String recherche = c.q() == null ? "" : c.q().trim().toLowerCase(Locale.ROOT);
        List<ClmDto.TrousseResume> res = new ArrayList<>();
        for (Trousse t : trousses.findByTenantIdOrderByMajLeDesc(ctx.tenant())) {
            if (!retenue(t, c) || (!recherche.isEmpty() && !correspond(t, recherche))) {
                continue;
            }
            if (lecture.visible(t, acteur)) {
                res.add(lecture.resume(t, parcours.etapesDe(t), maintenant));
            }
        }
        return res;
    }

    private static boolean retenue(Trousse t, ClmDto.CritereListe c) {
        java.time.LocalDate cree = java.time.LocalDate.ofInstant(t.getCreeLe(), java.time.ZoneOffset.UTC);
        return (vide(c.statut()) == null || t.getStatut().name().equals(c.statut()))
                && (vide(c.initiateur()) == null || t.getProprietaire().equalsIgnoreCase(c.initiateur().trim()))
                && (vide(c.type()) == null || c.type().trim().equalsIgnoreCase(t.getTypeContrat()))
                && (c.du() == null || !cree.isBefore(c.du()))
                && (c.au() == null || !cree.isAfter(c.au()));
    }

    private boolean correspond(Trousse t, String q) {
        if (contient(t.getReference(), q) || contient(t.getTitre(), q) || contient(t.getSfDossierId(), q) || contient(t.getProprietaire(), q)) {
            return true;
        }
        return participants.findByTenantIdAndTrousseIdOrderByIdAsc(t.getTenantId(), t.getId()).stream()
                .anyMatch(p -> contient(p.getNom(), q) || contient(p.getCourriel(), q));
    }

    private static boolean contient(String s, String q) {
        return s != null && s.toLowerCase(Locale.ROOT).contains(q);
    }

    public ClmDto.TrousseDetail detail(Long id, Habilitations.Acteur acteur) {
        return detail(lecture.exigerVisible(id, acteur), acteur);
    }

    ClmDto.TrousseDetail detail(Trousse t, Habilitations.Acteur acteur) {
        Instant maintenant = ctx.maintenant();
        List<EtapeTrousse> lesEtapes = parcours.etapesDe(t);
        Map<Long, String> noms = new LinkedHashMap<>();
        List<ClmDto.ParticipantVue> parts = new ArrayList<>();
        for (Participant p : participants.findByTenantIdAndTrousseIdOrderByIdAsc(t.getTenantId(), t.getId())) {
            noms.put(p.getId(), p.getNom());
            parts.add(LectureTrousse.participant(p));
        }
        List<ClmDto.InvitationVue> invs = new ArrayList<>();
        for (Invitation i : invitations.findByTenantIdAndTrousseIdOrderByIdAsc(t.getTenantId(), t.getId())) {
            invs.add(new ClmDto.InvitationVue(i.getId(), i.getParticipantId(), noms.get(i.getParticipantId()), i.getEtapeId(),
                    i.getStatut().name(), i.getNiveau().name(), i.getExpireLe(), i.getVerifieeLe(), i.getConsentementLe(),
                    i.getSigneeLe(), i.getMotif()));
        }
        List<String> alertes = new ArrayList<>();
        invitations.findByTenantIdAndTrousseIdOrderByIdAsc(t.getTenantId(), t.getId()).stream()
                .filter(i -> (i.getStatut() == Invitation.Statut.ENVOYEE || i.getStatut() == Invitation.Statut.VERIFIEE) && i.estExpiree(maintenant))
                .forEach(i -> alertes.add("Invitation expirée : " + noms.get(i.getParticipantId())));
        invitations.findByTenantIdAndTrousseIdOrderByIdAsc(t.getTenantId(), t.getId()).stream()
                .filter(i -> i.getStatut() == Invitation.Statut.BLOQUEE)
                .forEach(i -> alertes.add("Vérification d'identité bloquée : " + noms.get(i.getParticipantId())));
        List<Modele.Champ> aCompleter = champsDuModele(t).stream().filter(Modele.Champ::completeALaGeneration).toList();
        List<String> manquants = Preremplissage.appliquer(aCompleter, Map.of(), null, rawValeurs(t)).manquants();
        return new ClmDto.TrousseDetail(lecture.resume(t, lesEtapes, maintenant), t.getRisque().name(), t.getModeleId(), valeurs(t),
                aCompleter, manquants, t.getRappelFrequenceJours(), t.getRappelsMax(), t.getCopies(), t.getSfRecupereLe(),
                documents.findByTenantIdAndTrousseIdOrderByCleDocumentAscVersionDesc(t.getTenantId(), t.getId()).stream()
                        .map(LectureTrousse::document).toList(),
                parts, lesEtapes.stream().map(e -> lecture.etape(e, maintenant)).toList(), invs, actions(t, acteur), alertes);
    }

    /** Actions proposées par l'interface ; le serveur revérifie chacune à l'exécution. */
    static Set<String> actions(Trousse t, Habilitations.Acteur acteur) {
        Set<String> a = new LinkedHashSet<>();
        a.add("CONSULTER_PREUVE");
        if (!Habilitations.peutModifier(t, acteur)) {
            return a;
        }
        switch (t.getStatut()) {
            case BROUILLON -> a.addAll(List.of("MODIFIER", "AJOUTER_DOCUMENT", "PARTICIPANTS", "PARCOURS", "PREREMPLIR", "GENERER", "LANCER", "ANNULER"));
            // En cours de parcours : ajouter un signataire, corriger une donnée ; un document modifié ouvre un nouveau cycle.
            case EN_TRAITEMENT -> a.addAll(List.of("REASSIGNER", "PARTICIPANTS", "AJOUTER_DOCUMENT", "GENERER", "PREREMPLIR", "ANNULER"));
            case REJETEE -> a.addAll(List.of("REPRENDRE", "ANNULER"));
            default -> {
            }
        }
        return a;
    }

    public List<ClmDto.EvenementVue> historique(Long id, Habilitations.Acteur acteur) {
        Trousse t = lecture.exigerVisible(id, acteur);
        return evenements.findByTenantIdAndTrousseIdOrderBySequenceAsc(t.getTenantId(), t.getId()).stream()
                .map(LectureTrousse::evenement).toList();
    }

    List<DocumentTrousse> documentsCourants(Trousse t) {
        return documents.findByTenantIdAndTrousseIdOrderByCleDocumentAscVersionDesc(t.getTenantId(), t.getId()).stream()
                .filter(DocumentTrousse::estCourant).toList();
    }

    private static String exiger(String v, String message) {
        if (v == null || v.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return v.trim();
    }

    private static String vide(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
