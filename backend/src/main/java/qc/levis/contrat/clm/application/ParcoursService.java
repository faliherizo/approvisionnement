package qc.levis.contrat.clm.application;

import com.fasterxml.jackson.core.type.TypeReference;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.clm.application.dto.ClmDto;
import qc.levis.contrat.clm.application.port.RenduDocuments;
import qc.levis.contrat.clm.application.port.SourceAleatoire;
import qc.levis.contrat.clm.application.repository.DecisionEtapeRepository;
import qc.levis.contrat.clm.application.repository.DocumentTrousseRepository;
import qc.levis.contrat.clm.application.repository.EtapeTrousseRepository;
import qc.levis.contrat.clm.application.repository.EvenementTrousseRepository;
import qc.levis.contrat.clm.application.repository.InvitationRepository;
import qc.levis.contrat.clm.application.repository.ModeleContratRepository;
import qc.levis.contrat.clm.application.repository.ParticipantRepository;
import qc.levis.contrat.clm.application.repository.TrousseRepository;
import qc.levis.contrat.clm.domain.ChaineScellee;
import qc.levis.contrat.clm.domain.DecisionEtape;
import qc.levis.contrat.clm.domain.DocumentTrousse;
import qc.levis.contrat.clm.domain.EtapeTrousse;
import qc.levis.contrat.clm.domain.FormatDocument;
import qc.levis.contrat.clm.domain.Habilitations;
import qc.levis.contrat.clm.domain.Invitation;
import qc.levis.contrat.clm.domain.Langue;
import qc.levis.contrat.clm.domain.Modele;
import qc.levis.contrat.clm.domain.ModeleMessage.Evenement;
import qc.levis.contrat.clm.domain.ParametresClm;
import qc.levis.contrat.clm.domain.Parcours;
import qc.levis.contrat.clm.domain.Participant;
import qc.levis.contrat.clm.domain.Relances;
import qc.levis.contrat.clm.domain.StatutTrousse;
import qc.levis.contrat.clm.domain.Trousse;

/**
 * Exécution du parcours d'une trousse (ST-07/ST-08/ST-09) : activation des phases, décisions avec quorum,
 * réassignation à chaud, invitations de signature, rejet, retour pour correction et finalisation.
 */
@Service
public class ParcoursService {

    private static final TypeReference<List<Modele.Champ>> CHAMPS = new TypeReference<>() {
    };

    private final TrousseRepository trousses;
    private final EtapeTrousseRepository etapes;
    private final DecisionEtapeRepository decisions;
    private final EvenementTrousseRepository evenements;
    private final ParticipantRepository participants;
    private final InvitationRepository invitations;
    private final DocumentTrousseRepository documents;
    private final ModeleContratRepository modeles;
    private final RenduDocuments rendu;
    private final MessagerieClm messagerie;
    private final DiffusionStatut diffusion;
    private final JournalClm journal;
    private final SourceAleatoire alea;
    private final ParametresClmService parametres;
    private final LectureTrousse lecture;
    private final ContexteClm ctx;

    public ParcoursService(TrousseRepository trousses, EtapeTrousseRepository etapes, DecisionEtapeRepository decisions,
                           EvenementTrousseRepository evenements,
                           ParticipantRepository participants, InvitationRepository invitations, DocumentTrousseRepository documents,
                           ModeleContratRepository modeles, RenduDocuments rendu, MessagerieClm messagerie,
                           DiffusionStatut diffusion, JournalClm journal, SourceAleatoire alea, ParametresClmService parametres,
                           LectureTrousse lecture, ContexteClm ctx) {
        this.trousses = trousses;
        this.etapes = etapes;
        this.decisions = decisions;
        this.evenements = evenements;
        this.participants = participants;
        this.invitations = invitations;
        this.documents = documents;
        this.modeles = modeles;
        this.rendu = rendu;
        this.messagerie = messagerie;
        this.diffusion = diffusion;
        this.journal = journal;
        this.alea = alea;
        this.parametres = parametres;
        this.lecture = lecture;
        this.ctx = ctx;
    }

    // --- Moteur ---
    /** Une étape dont la règle d'affaires n'est pas satisfaite par les valeurs de la trousse est écartée (F-05). */
    void demarrer(Trousse t, String acteur) {
        reinitialiserDepuis(t, Integer.MIN_VALUE);
        avancer(t, acteur);
    }

    /**
     * Valeurs offertes aux règles d'affaires d'une étape : les champs de la trousse, et ses attributs de processus
     * (addenda 4, Q8 et Q20) — type de document, unité administrative, niveau de risque et origine — pour acheminer selon
     * le processus, l'unité ou le montant sans dupliquer ces données dans un champ du modèle.
     */
    static final String TYPE = "trousse.typeContrat";
    static final String UNITE = "trousse.unite";
    static final String RISQUE = "trousse.risque";
    static final String SOURCE = "trousse.source";
    /** Attributs de la trousse admis dans une règle d'étape, en plus des champs du modèle. */
    static final java.util.Set<String> ATTRIBUTS_TROUSSE = java.util.Set.of(TYPE, UNITE, RISQUE, SOURCE);

    Map<String, Object> valeursDeRegle(Trousse t) {
        Map<String, Object> valeurs = new HashMap<>(ctx.valeursBrutes(t.getValeursJson()));
        valeurs.put(TYPE, t.getTypeContrat() == null ? "" : t.getTypeContrat());
        valeurs.put(UNITE, t.getUnite() == null ? "" : t.getUnite());
        valeurs.put(RISQUE, t.getRisque().name());
        valeurs.put(SOURCE, t.getSource().name());
        return valeurs;
    }

    /** Remet en attente les étapes à partir d'une phase, en réappliquant leurs règles d'affaires aux valeurs actuelles. */
    private void reinitialiserDepuis(Trousse t, int phase) {
        Map<String, Object> valeurs = valeursDeRegle(t);
        Instant maintenant = ctx.maintenant();
        for (EtapeTrousse e : etapesDe(t)) {
            if (e.getPhase() < phase) {
                continue;
            }
            for (Invitation i : invitations.findByTenantIdAndEtapeId(t.getTenantId(), e.getId())) {
                i.invalider();
                invitations.save(i);
            }
            e.reinitialiser();
            List<Modele.Condition> regle = ctx.conditions(e.getConditionsJson());
            if (!regle.isEmpty() && !ReglesClm.satisfaites(regle, valeurs)) {
                e.ecarter(maintenant);
                journal.consigner(t, "ETAPE_NON_REQUISE", "systeme", e.getLibelle() + " · règle d'affaires non satisfaite");
            }
            etapes.save(e);
        }
    }

    /**
     * Modification d'un document en cours de parcours (scénario 5) : les décisions et signatures déjà recueillies portaient
     * sur l'ancienne version. Un nouveau cycle repart de la première phase ; l'historique du cycle précédent reste au journal.
     */
    void nouveauCycle(Trousse t, String acteur, String motif) {
        if (!t.enTraitement()) {
            return;
        }
        reinitialiserDepuis(t, Integer.MIN_VALUE);
        journal.consigner(t, "NOUVEAU_CYCLE", acteur, motif);
        notifierProprietaire(t, Evenement.REJET, "Nouveau cycle : " + motif);
        avancer(t, acteur);
    }

    void avancer(Trousse t, String acteur) {
        List<EtapeTrousse> liste = etapesDe(t);
        Parcours.Avancement a = Parcours.avancer(liste.stream()
                .map(e -> new Parcours.EtatEtape(e.getId(), e.getPhase(), e.getStatut())).toList());
        if (a.resultat().isPresent()) {
            if (a.resultat().get() == StatutTrousse.TERMINEE) {
                finaliser(t, acteur);
            } else {
                rejeter(t, acteur, "Étape rejetée");
            }
            return;
        }
        Instant maintenant = ctx.maintenant();
        for (EtapeTrousse e : liste) {
            if (a.aActiver().contains(e.getId())) {
                e.activer(maintenant);
                etapes.save(e);
                journal.consigner(t, "ETAPE_ACTIVEE", acteur, e.getLibelle());
                notifierActivation(t, e);
            }
        }
    }

    private void notifierActivation(Trousse t, EtapeTrousse e) {
        List<Parcours.Affectation> affs = ctx.affectations(e.getAffectationsJson());
        if (e.getType() == Parcours.TypeEtape.SIGNATURE) {
            for (Parcours.Affectation a : affs) {
                participants.findByIdAndTenantId(Habilitations.parseId(a.valeur()), t.getTenantId())
                        .ifPresent(p -> inviter(t, e, p));
            }
            return;
        }
        for (Parcours.Affectation a : affs) {
            notifierTache(t, e, a, Evenement.TACHE);
        }
    }

    void notifierTache(Trousse t, EtapeTrousse e, Parcours.Affectation a, Evenement evenement) {
        Map<String, Object> vars = variables(t, e);
        switch (a.type()) {
            case UTILISATEUR -> messagerie.envoyer(t.getTenantId(), evenement, "COURRIEL", a.valeur(), vars, List.of());
            case GROUPE, ROLE -> messagerie.envoyer(t.getTenantId(), evenement, "INTERNE",
                    a.type().name().toLowerCase() + ":" + a.valeur(), vars, List.of());
            case PARTICIPANT -> participants.findByIdAndTenantId(Habilitations.parseId(a.valeur()), t.getTenantId())
                    .ifPresent(p -> messagerie.envoyer(t.getTenantId(), evenement, "COURRIEL", p.getCourriel(), vars, List.of(), p.getLangue()));
        }
    }

    Map<String, Object> variables(Trousse t, EtapeTrousse e) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("reference", t.getReference());
        vars.put("titre", t.getTitre());
        vars.put("etape", e == null ? "" : e.getLibelle());
        vars.put("echeance", e == null || e.getEcheance() == null ? "—" : e.getEcheance().toString().substring(0, 10));
        return vars;
    }

    /** Crée l'invitation et envoie le lien ; le jeton en clair ne vit que dans le message. */
    String inviter(Trousse t, EtapeTrousse e, Participant p) {
        ParametresClm.Reglages reglages = parametres.reglages(t.getTenantId());
        String jeton = alea.jeton();
        Instant expire = ctx.maintenant().plus(Duration.ofDays(reglages.invitationValiditeJours()));
        invitations.save(new Invitation(t.getTenantId(), t.getId(), e.getId(), p.getId(), ChaineScellee.sha256(jeton.getBytes(
                java.nio.charset.StandardCharsets.UTF_8)), expire, p.getNiveauIdentite()));
        Map<String, Object> vars = variables(t, e);
        vars.put("destinataire", p.getNom());
        vars.put("lien", lienPortail(reglages, jeton, p.getLangue()));
        vars.put("echeance", expire.toString().substring(0, 10));
        messagerie.envoyer(t.getTenantId(), Evenement.INVITATION, "COURRIEL", p.getCourriel(), vars, List.of(), p.getLangue());
        journal.consigner(t, "INVITATION_ENVOYEE", "systeme", p.getNom() + " · " + p.getNiveauIdentite());
        return jeton;
    }

    // --- Décisions de validation et d'approbation (UI-13/UI-14) ---
    @Transactional
    public ClmDto.EtapeVue decider(Long etapeId, Habilitations.Acteur acteur, ClmDto.DecisionRequest req) {
        EtapeTrousse e = exigerEtape(etapeId);
        Trousse t = lecture.exiger(e.getTrousseId());
        if (e.getType() == Parcours.TypeEtape.SIGNATURE) {
            throw new IllegalStateException("Une signature se fait depuis le portail de signature.");
        }
        exigerDecidable(t, e, acteur);
        DecisionEtape.Nature nature = nature(req.decision());
        if (nature != DecisionEtape.Nature.APPROUVE && (req.commentaire() == null || req.commentaire().isBlank())) {
            throw new IllegalArgumentException("Motif obligatoire en cas de rejet ou de retour.");
        }
        Instant maintenant = ctx.maintenant();
        decisions.save(new DecisionEtape(t.getTenantId(), t.getId(), e.getId(), acteur.id(), nature, req.commentaire(), maintenant));
        journal.consigner(t, "DECISION_" + nature.name(), acteur.id(), e.getLibelle()
                + (req.commentaire() == null ? "" : " · " + req.commentaire()));
        if (nature == DecisionEtape.Nature.RETOUR && req.retourVers() != null && !req.retourVers().isBlank()) {
            retournerAEtape(t, e, req.retourVers().trim(), acteur.id(), req.commentaire());
        } else if (nature == DecisionEtape.Nature.RETOUR) {
            e.terminer(Parcours.StatutEtape.REJETEE, maintenant);
            etapes.save(e);
            retourner(t, acteur.id(), req.commentaire());
        } else {
            recalculer(t, e, acteur.id());
        }
        return lecture.etape(e, maintenant);
    }

    /**
     * Retour à l'étape appropriée (scénario 9) : le document revient à une étape antérieure désignée ; le parcours reprend
     * depuis sa phase, sans repasser par le brouillon, et les décisions du cycle précédent restent à l'historique.
     */
    private void retournerAEtape(Trousse t, EtapeTrousse courante, String cle, String acteur, String motif) {
        EtapeTrousse cible = etapesDe(t).stream().filter(x -> x.getCle().equals(cle)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Étape de retour inconnue : " + cle));
        if (cible.getPhase() >= courante.getPhase()) {
            throw new IllegalArgumentException("Le retour se fait vers une étape antérieure à « " + courante.getLibelle() + " ».");
        }
        reinitialiserDepuis(t, cible.getPhase());
        long seq = journal.consigner(t, "RETOUR_ETAPE", acteur, courante.getLibelle() + " → " + cible.getLibelle() + " · " + motif)
                .getSequence();
        notifierProprietaire(t, Evenement.REJET, "Retour à l'étape « " + cible.getLibelle() + " » : " + motif);
        // Addenda 2, Q14 : « retournée » est un statut que Salesforce doit recevoir, quelle que soit l'étape visée.
        diffusion.enfiler(t, "RETOURNEE", seq, Map.of("motif", motif, "etape", cible.getCle()));
        avancer(t, acteur);
    }

    void exigerDecidable(Trousse t, EtapeTrousse e, Habilitations.Acteur acteur) {
        if (t.getStatut() != StatutTrousse.EN_TRAITEMENT) {
            throw new IllegalStateException("Dossier suspendu : la trousse n'est pas en traitement.");
        }
        if (e.getStatut() != Parcours.StatutEtape.ACTIVE) {
            throw new IllegalStateException("Tâche déjà traitée ou non encore active.");
        }
        boolean designe = ctx.affectations(e.getAffectationsJson()).stream()
                .anyMatch(a -> Habilitations.correspond(a, acteur, lecture.courriels(t)));
        if (!designe) {
            throw new org.springframework.security.access.AccessDeniedException("Cette tâche ne vous est pas assignée.");
        }
        Habilitations.exigerSeparation(t, e.getType(), acteur);
        boolean dejaDecide = decisions.duCycleEnCours(e).stream()
                .anyMatch(d -> d.getActeur().equalsIgnoreCase(acteur.id()));
        if (dejaDecide) {
            throw new IllegalStateException("Approbation déjà faite par vous sur cette étape.");
        }
    }

    /** Recalcule une étape active après une décision ou une réassignation, puis fait avancer le parcours. */
    void recalculer(Trousse t, EtapeTrousse e, String acteur) {
        List<DecisionEtape> ds = decisions.duCycleEnCours(e);
        int approbations = (int) ds.stream().filter(d -> d.getNature() == DecisionEtape.Nature.APPROUVE).count();
        int refus = (int) ds.stream().filter(d -> d.getNature() == DecisionEtape.Nature.REJETE).count();
        Parcours.StatutEtape statut = Parcours.statutApresDecisions(e.getQuorum(), e.getPolitique(),
                capacite(ctx.affectations(e.getAffectationsJson())), approbations, refus);
        if (statut == Parcours.StatutEtape.ACTIVE) {
            return;
        }
        e.terminer(statut, ctx.maintenant());
        etapes.save(e);
        journal.consigner(t, statut == Parcours.StatutEtape.APPROUVEE ? "ETAPE_APPROUVEE" : "ETAPE_REJETEE", acteur, e.getLibelle());
        if (statut == Parcours.StatutEtape.APPROUVEE) {
            annulerInvitationsOuvertes(t, e);
        }
        avancer(t, acteur);
    }

    /** Les groupes et rôles ont un effectif inconnu ici : on les suppose assez nombreux pour le quorum. */
    static int capacite(List<Parcours.Affectation> affs) {
        boolean collectif = affs.stream().anyMatch(a -> a.type() == Parcours.TypeAffecte.GROUPE || a.type() == Parcours.TypeAffecte.ROLE);
        return collectif ? Integer.MAX_VALUE / 2 : affs.size();
    }

    private static DecisionEtape.Nature nature(String decision) {
        try {
            return DecisionEtape.Nature.valueOf(decision == null ? "" : decision.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Décision attendue : APPROUVE, REJETE ou RETOUR.");
        }
    }

    // --- Signatures (appelé par le portail) ---
    void enregistrerSignature(Trousse t, EtapeTrousse e, Participant p, boolean signe, String motif) {
        Instant maintenant = ctx.maintenant();
        decisions.save(new DecisionEtape(t.getTenantId(), t.getId(), e.getId(), p.getCourriel(),
                signe ? DecisionEtape.Nature.APPROUVE : DecisionEtape.Nature.REJETE, motif, maintenant));
        recalculer(t, e, p.getCourriel());
    }

    // --- Réassignation, ajout, retrait en cours (ST-08) ---
    @Transactional
    public ClmDto.EtapeVue modifierAffectations(Long etapeId, Habilitations.Acteur acteur, ClmDto.AffectationsRequest req) {
        EtapeTrousse e = exigerEtape(etapeId);
        Trousse t = lecture.exigerGestion(e.getTrousseId(), acteur);
        e.exigerModifiable();
        if (t.getStatut().estFinal()) {
            throw new IllegalStateException("Trousse close.");
        }
        List<Parcours.Affectation> nouvelles = TrousseService.affectations(req.affectations());
        int quorum = req.quorum() == null ? e.getQuorum() : req.quorum();
        List<String> erreurs = Parcours.valider(List.of(new Parcours.DefinitionEtape(e.getCle(), e.getLibelle(), 1, e.getType(),
                nouvelles, quorum, e.getPolitique(), e.getDelaiJours())));
        if (!erreurs.isEmpty()) {
            throw new IllegalArgumentException(String.join(" ", erreurs));
        }
        List<Parcours.Affectation> anciennes = ctx.affectations(e.getAffectationsJson());
        e.setAffectationsJson(ctx.ecrire(nouvelles));
        e.setQuorum(quorum);
        etapes.save(e);
        journal.consigner(t, "REASSIGNATION", acteur.id(), e.getLibelle() + " : " + resumer(anciennes) + " → " + resumer(nouvelles)
                + (req.motif() == null || req.motif().isBlank() ? "" : " · " + req.motif()));
        if (e.getStatut() == Parcours.StatutEtape.ACTIVE) {
            appliquerAChaud(t, e, anciennes, nouvelles);
            recalculer(t, e, acteur.id());
        }
        return lecture.etape(e, ctx.maintenant());
    }

    private void appliquerAChaud(Trousse t, EtapeTrousse e, List<Parcours.Affectation> anciennes, List<Parcours.Affectation> nouvelles) {
        if (e.getType() == Parcours.TypeEtape.SIGNATURE) {
            Set<String> gardes = nouvelles.stream().map(Parcours.Affectation::valeur).collect(Collectors.toSet());
            for (Invitation inv : invitations.findByTenantIdAndEtapeId(t.getTenantId(), e.getId())) {
                if (!gardes.contains(String.valueOf(inv.getParticipantId()))) {
                    inv.annuler();
                    invitations.save(inv);
                }
            }
        }
        for (Parcours.Affectation a : nouvelles) {
            if (anciennes.contains(a)) {
                continue;
            }
            if (e.getType() == Parcours.TypeEtape.SIGNATURE) {
                participants.findByIdAndTenantId(Habilitations.parseId(a.valeur()), t.getTenantId()).ifPresent(p -> inviter(t, e, p));
            } else {
                notifierTache(t, e, a, Evenement.TACHE);
            }
        }
    }

    private static String resumer(List<Parcours.Affectation> affs) {
        return affs.stream().map(a -> a.type().name().charAt(0) + ":" + a.valeur()).collect(Collectors.joining(", "));
    }

    // --- Issues du parcours ---
    void rejeter(Trousse t, String acteur, String motif) {
        t.changerStatut(StatutTrousse.REJETEE, ctx.maintenant());
        trousses.save(t);
        annulerInvitations(t);
        long seq = journal.consigner(t, "TROUSSE_REJETEE", acteur, motif).getSequence();
        notifierProprietaire(t, Evenement.REJET, motif);
        diffusion.enfiler(t, "REJETEE", seq, Map.of("motif", motif == null ? "" : motif));
    }

    void retourner(Trousse t, String acteur, String motif) {
        t.changerStatut(StatutTrousse.BROUILLON, ctx.maintenant());
        trousses.save(t);
        for (EtapeTrousse e : etapesDe(t)) {
            e.reinitialiser();
            etapes.save(e);
        }
        annulerInvitations(t);
        for (DocumentTrousse d : documents.findByTenantIdAndTrousseIdOrderByCleDocumentAscVersionDesc(t.getTenantId(), t.getId())) {
            if (d.getStatut() == DocumentTrousse.Statut.ENVOYE) {
                d.setStatut(DocumentTrousse.Statut.BROUILLON);
                documents.save(d);
            }
        }
        long seq = journal.consigner(t, "RETOUR_CORRECTION", acteur, motif).getSequence();
        notifierProprietaire(t, Evenement.REJET, "Retour pour correction : " + motif);
        diffusion.enfiler(t, "RETOURNEE", seq, Map.of("motif", motif == null ? "" : motif));
    }

    void finaliser(Trousse t, String acteur) {
        Instant maintenant = ctx.maintenant();
        List<Invitation> signees = invitations.findByTenantIdAndTrousseIdOrderByIdAsc(t.getTenantId(), t.getId()).stream()
                .filter(i -> i.getStatut() == Invitation.Statut.SIGNEE).toList();
        List<Modele.Champ> zones = zonesSignature(t);
        for (DocumentTrousse d : documents.findByTenantIdAndTrousseIdOrderByCleDocumentAscVersionDesc(t.getTenantId(), t.getId())) {
            if (d.getStatut() == DocumentTrousse.Statut.ENVOYE) {
                verrouiller(t, d, signees, zones, maintenant);
            }
        }
        t.changerStatut(StatutTrousse.TERMINEE, maintenant);
        trousses.save(t);
        long seq = journal.consigner(t, "TROUSSE_TERMINEE", acteur, signees.size() + " signature(s)").getSequence();
        notifierProprietaire(t, Evenement.TERMINEE, "");
        diffusion.enfiler(t, "SIGNATURE_TERMINEE", seq, Map.of("signatures", signees.size(), "termineeLe", maintenant.toString()));
    }

    private void verrouiller(Trousse t, DocumentTrousse d, List<Invitation> signees, List<Modele.Champ> zones, Instant maintenant) {
        if (d.getType() != FormatDocument.Type.PDF) {
            d.setStatut(DocumentTrousse.Statut.SIGNE);
            documents.save(d);
            return;
        }
        List<RenduDocuments.Apposition> appositions = new ArrayList<>();
        Map<Long, Participant> parId = new HashMap<>();
        participants.findByTenantIdAndTrousseIdOrderByIdAsc(t.getTenantId(), t.getId()).forEach(p -> parId.put(p.getId(), p));
        for (Invitation i : signees) {
            Participant p = parId.get(i.getParticipantId());
            Optional<Modele.Champ> zone = zones.stream().filter(z -> p != null && p.getRoleContrat().equals(z.roleDestinataire())).findFirst();
            appositions.add(new RenduDocuments.Apposition(i.getNomSigne(), i.getSigneeLe().toString(), t.getReference(),
                    zone.map(Modele.Champ::position).orElse(null)));
        }
        byte[] signe = rendu.apposer(d.getContenu(), appositions);
        d.setStatut(DocumentTrousse.Statut.REMPLACE);
        documents.save(d);
        DocumentTrousse finale = new DocumentTrousse(t.getTenantId(), t.getId(), d.getCleDocument(), d.getVersion() + 1, d.getNom(),
                d.getType(), signe, "systeme", "Version signée (source v" + d.getVersion() + " · " + d.getEmpreinte().substring(0, 12) + ")", maintenant);
        finale.setStatut(DocumentTrousse.Statut.SIGNE);
        finale.setAnalyse(DocumentTrousse.Analyse.PROPRE);
        documents.save(finale);
    }

    List<Modele.Champ> zonesSignature(Trousse t) {
        if (t.getModeleId() == null) {
            return List.of();
        }
        return modeles.findByIdAndTenantId(t.getModeleId(), t.getTenantId())
                .map(m -> ctx.lire(m.getChampsJson(), CHAMPS, List.<Modele.Champ>of()))
                .orElse(List.of()).stream().filter(c -> c.type() == Modele.TypeChamp.SIGNATURE && c.position() != null).toList();
    }

    void annulerInvitations(Trousse t) {
        for (Invitation i : invitations.findByTenantIdAndTrousseIdOrderByIdAsc(t.getTenantId(), t.getId())) {
            i.annuler();
            invitations.save(i);
        }
    }

    private void annulerInvitationsOuvertes(Trousse t, EtapeTrousse e) {
        for (Invitation i : invitations.findByTenantIdAndEtapeId(t.getTenantId(), e.getId())) {
            i.annuler();
            invitations.save(i);
        }
    }

    void notifierProprietaire(Trousse t, Evenement evenement, String motif) {
        Map<String, Object> vars = variables(t, null);
        vars.put("motif", motif);
        List<String> copies = t.getCopies() == null ? List.of() : java.util.Arrays.stream(t.getCopies().split("[,;\\s]+"))
                .filter(s -> !s.isBlank()).toList();
        messagerie.envoyer(t.getTenantId(), evenement, "COURRIEL", t.getProprietaire(), vars, copies);
    }

    // --- Boîte de tâches (UI-12) ---
    public List<ClmDto.TacheVue> mesTaches(Habilitations.Acteur acteur, String filtre) {
        Long tenant = ctx.tenant();
        Instant maintenant = ctx.maintenant();
        List<ClmDto.TacheVue> res = new ArrayList<>();
        if ("terminees".equals(filtre)) {
            for (DecisionEtape d : decisions.findTop50ByTenantIdAndActeurOrderByDecideLeDesc(tenant, acteur.id())) {
                etapes.findByIdAndTenantId(d.getEtapeId(), tenant).ifPresent(e ->
                        res.add(tache(lecture.exiger(e.getTrousseId()), e, maintenant, true, d.getNature().name())));
            }
            return res;
        }
        for (EtapeTrousse e : etapes.findByTenantIdAndStatut(tenant, Parcours.StatutEtape.ACTIVE)) {
            Trousse t = lecture.exiger(e.getTrousseId());
            Map<Long, String> courriels = lecture.courriels(t);
            boolean designe = ctx.affectations(e.getAffectationsJson()).stream().anyMatch(a -> Habilitations.correspond(a, acteur, courriels));
            boolean dejaDecide = decisions.duCycleEnCours(e).stream()
                    .anyMatch(d -> d.getActeur().equalsIgnoreCase(acteur.id()));
            if (!designe || dejaDecide) {
                continue;
            }
            ClmDto.TacheVue v = tache(t, e, maintenant, false, null);
            boolean retenue = switch (filtre == null ? "" : filtre) {
                case "en_retard" -> v.enRetard();
                case "deleguees" -> v.reassignee();
                default -> true;
            };
            if (retenue) {
                res.add(v);
            }
        }
        res.sort(Comparator.comparing((ClmDto.TacheVue v) -> !v.enRetard())
                .thenComparing(ClmDto.TacheVue::echeance, Comparator.nullsLast(Comparator.naturalOrder())));
        return res;
    }

    private ClmDto.TacheVue tache(Trousse t, EtapeTrousse e, Instant maintenant, boolean decidee, String maDecision) {
        return new ClmDto.TacheVue(e.getId(), t.getId(), t.getReference(), t.getTitre(), e.getType().name(), e.getLibelle(),
                t.getProprietaire(), e.getEcheance(), e.getStatut() == Parcours.StatutEtape.ACTIVE && Relances.enRetard(e.getEcheance(), maintenant),
                e.getStatut().name(), decidee, maDecision, reassignee(t, e));
    }

    /** Une étape réassignée ou déléguée porte un événement dont le détail commence par son libellé (voir les consignations). */
    private boolean reassignee(Trousse t, EtapeTrousse e) {
        String prefixe = e.getLibelle() + " :";
        return evenements.findByTenantIdAndTrousseIdOrderBySequenceAsc(t.getTenantId(), t.getId()).stream()
                .anyMatch(ev -> ("REASSIGNATION".equals(ev.getType()) || "DELEGATION".equals(ev.getType()))
                        && ev.getDetail() != null && ev.getDetail().startsWith(prefixe));
    }

    // --- Relance, commentaire et délégation (UI-04, UI-13, UI-14) ---
    /** Relance manuelle par l'initiateur : rappel aux intervenants en attente, nouveau lien pour les signataires. */
    @Transactional
    public ClmDto.EtapeVue relancerEtape(Long etapeId, Habilitations.Acteur acteur) {
        EtapeTrousse e = exigerEtape(etapeId);
        Trousse t = lecture.exigerGestion(e.getTrousseId(), acteur);
        if (e.getStatut() != Parcours.StatutEtape.ACTIVE) {
            throw new IllegalStateException("Seule une étape active peut être relancée.");
        }
        int destinataires = 0;
        if (e.getType() == Parcours.TypeEtape.SIGNATURE) {
            for (Invitation i : invitations.findByTenantIdAndEtapeId(t.getTenantId(), e.getId())) {
                if (i.getStatut() == Invitation.Statut.SIGNEE || i.getStatut() == Invitation.Statut.REFUSEE || i.getStatut() == Invitation.Statut.ANNULEE) {
                    continue;
                }
                i.annuler();
                invitations.save(i);
                Optional<Participant> p = participants.findByIdAndTenantId(i.getParticipantId(), t.getTenantId());
                if (p.isPresent()) {
                    inviter(t, e, p.get());
                    destinataires++;
                }
            }
        } else {
            List<String> ontDecide = decisions.duCycleEnCours(e).stream()
                    .map(d -> d.getActeur().toLowerCase(java.util.Locale.ROOT)).toList();
            for (Parcours.Affectation a : ctx.affectations(e.getAffectationsJson())) {
                if (a.type() != Parcours.TypeAffecte.UTILISATEUR || !ontDecide.contains(a.valeur().toLowerCase(java.util.Locale.ROOT))) {
                    notifierTache(t, e, a, Evenement.RAPPEL);
                    destinataires++;
                }
            }
        }
        e.noterRappel(ctx.maintenant());
        etapes.save(e);
        journal.consigner(t, "RELANCE_MANUELLE", acteur.id(), e.getLibelle() + " · " + destinataires + " destinataire(s)");
        return lecture.etape(e, ctx.maintenant());
    }

    /** Commentaire contextualisé sur une étape, sans décision : il rejoint le journal scellé de la trousse. */
    @Transactional
    public void commenter(Long etapeId, Habilitations.Acteur acteur, ClmDto.CommentaireRequest req) {
        EtapeTrousse e = exigerEtape(etapeId);
        Trousse t = lecture.exigerContribution(e.getTrousseId(), acteur);
        String texte = req == null || req.texte() == null ? "" : req.texte().trim();
        if (texte.isEmpty()) {
            throw new IllegalArgumentException("Commentaire vide.");
        }
        if (texte.length() > 1500) {
            throw new IllegalArgumentException("Commentaire trop long (1500 caractères au plus).");
        }
        journal.consigner(t, "COMMENTAIRE", acteur.id(), e.getLibelle() + " : " + texte);
    }

    /** Délégation par l'intervenant désigné nominativement ; la séparation des responsabilités reste appliquée. */
    @Transactional
    public ClmDto.EtapeVue deleguer(Long etapeId, Habilitations.Acteur acteur, ClmDto.DelegationRequest req) {
        EtapeTrousse e = exigerEtape(etapeId);
        Trousse t = lecture.exiger(e.getTrousseId());
        e.exigerModifiable();
        if (e.getType() == Parcours.TypeEtape.SIGNATURE) {
            throw new IllegalStateException("Une signature ne se délègue pas : le signataire est désigné nominativement.");
        }
        String vers = req == null || req.vers() == null ? "" : req.vers().trim().toLowerCase(java.util.Locale.ROOT);
        if (!vers.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("Délégataire invalide : courriel attendu.");
        }
        if (vers.equalsIgnoreCase(acteur.id())) {
            throw new IllegalArgumentException("Impossible de se déléguer une tâche à soi-même.");
        }
        if (vers.equalsIgnoreCase(t.getProprietaire())) {
            throw new IllegalStateException("Séparation des responsabilités : l'initiateur ne peut pas recevoir cette tâche.");
        }
        List<Parcours.Affectation> affs = new ArrayList<>(ctx.affectations(e.getAffectationsJson()));
        int moi = -1;
        for (int i = 0; i < affs.size(); i++) {
            Parcours.Affectation a = affs.get(i);
            if (a.type() == Parcours.TypeAffecte.UTILISATEUR && a.valeur().equalsIgnoreCase(vers)) {
                throw new IllegalStateException("Le délégataire est déjà désigné sur cette étape.");
            }
            if (a.type() == Parcours.TypeAffecte.UTILISATEUR && a.valeur().equalsIgnoreCase(acteur.id())) {
                moi = i;
            }
        }
        if (moi < 0) {
            throw new org.springframework.security.access.AccessDeniedException("Seule une tâche qui vous est assignée nominativement peut être déléguée.");
        }
        affs.set(moi, new Parcours.Affectation(Parcours.TypeAffecte.UTILISATEUR, vers));
        e.setAffectationsJson(ctx.ecrire(affs));
        etapes.save(e);
        String motif = req.motif() == null || req.motif().isBlank() ? "" : " · " + req.motif().trim();
        journal.consigner(t, "DELEGATION", acteur.id(), e.getLibelle() + " : " + acteur.id() + " → " + vers + motif);
        if (e.getStatut() == Parcours.StatutEtape.ACTIVE) {
            notifierTache(t, e, affs.get(moi), Evenement.TACHE);
        }
        return lecture.etape(e, ctx.maintenant());
    }

    /** Signataire interne authentifié : un nouveau lien remplace l'ancien, dont le jeton n'est pas conservé. */
    @Transactional
    public String lienSignature(Long etapeId, Habilitations.Acteur acteur) {
        EtapeTrousse e = exigerEtape(etapeId);
        Trousse t = lecture.exiger(e.getTrousseId());
        if (e.getType() != Parcours.TypeEtape.SIGNATURE || e.getStatut() != Parcours.StatutEtape.ACTIVE) {
            throw new IllegalStateException("Aucune signature en attente sur cette étape.");
        }
        Participant moi = participants.findByTenantIdAndTrousseIdOrderByIdAsc(t.getTenantId(), t.getId()).stream()
                .filter(p -> p.getCourriel().equalsIgnoreCase(acteur.id()) && !p.isExterne()).findFirst()
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("Cette signature ne vous est pas assignée."));
        for (Invitation i : invitations.findByTenantIdAndEtapeId(t.getTenantId(), e.getId())) {
            if (i.getParticipantId().equals(moi.getId())) {
                if (i.getStatut() == Invitation.Statut.SIGNEE) {
                    throw new IllegalStateException("Signature déjà apposée.");
                }
                i.annuler();
                invitations.save(i);
            }
        }
        return lienPortail(parametres.reglages(t.getTenantId()), inviter(t, e, moi), moi.getLangue());
    }

    /** Le portail s'ouvre dans la langue du participant ; le français reste celui par défaut (addenda 2, Q19). */
    static String lienPortail(ParametresClm.Reglages reglages, String jeton, Langue langue) {
        String portail = reglages.portailUrl();
        return (portail.endsWith("/") ? portail : portail + "/") + jeton + (langue == Langue.EN ? "?lang=en" : "");
    }

    EtapeTrousse exigerEtape(Long id) {
        return etapes.findByIdAndTenantId(id, ctx.tenant()).orElseThrow(() -> new NoSuchElementException("Étape introuvable : " + id));
    }

    List<EtapeTrousse> etapesDe(Trousse t) {
        return etapes.findByTenantIdAndTrousseIdOrderByPhaseAscIdAsc(t.getTenantId(), t.getId());
    }
}
