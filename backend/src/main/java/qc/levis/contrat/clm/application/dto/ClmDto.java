package qc.levis.contrat.clm.application.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import qc.levis.contrat.clm.domain.Modele;
import qc.levis.contrat.clm.domain.Preremplissage;

/** Contrats d'échange de l'API CLM (requêtes et vues), regroupés pour rester lisibles d'un seul tenant. */
public final class ClmDto {

    private ClmDto() {
    }

    // --- Requêtes ---
    public record TrousseRequest(String reference, String titre, String typeContrat, String unite, String risque,
                                 LocalDate echeance, Long modeleId, String sfDossierId, Integer rappelFrequenceJours,
                                 Integer rappelsMax, String copies) {
    }

    public record ParticipantRequest(String nom, String courriel, String telephone, String roleContrat, boolean externe,
                                     String niveauIdentite, String question, String reponse) {
    }

    public record AffectationDto(String type, String valeur) {
    }

    public record EtapeRequest(String cle, String libelle, int phase, String type, List<AffectationDto> affectations,
                               Integer quorum, String politique, Integer delaiJours, List<Modele.Condition> conditions) {
    }

    public record DecisionRequest(String decision, String commentaire) {
    }

    public record AffectationsRequest(List<AffectationDto> affectations, Integer quorum, String motif) {
    }

    public record PreremplissageRequest(String sfDossierId, Map<String, Object> valeursManuelles) {
    }

    /** Valeurs saisies ou corrigées par l'initiateur ; une valeur vide retire la saisie. */
    public record ValeursRequest(Map<String, Object> valeurs) {
    }

    /** Zone de signature attribuée à un participant, désignée par son rôle dans le modèle. */
    public record ZoneRequest(String roleContrat) {
    }

    public record ModeleRequest(String nom, String categorie, String groupes, String corps, List<Modele.Champ> champs,
                                List<Modele.Clause> clauses, List<EtapeRequest> parcours, Long verrou) {
    }

    public record MessageRequest(String evenement, String canal, String sujet, String corps, Boolean actif) {
    }

    public record TestMessageRequest(String canal, String destinataire) {
    }

    public record RevisionRequest(boolean valide, String justification) {
    }

    public record CommentaireRequest(String texte) {
    }

    public record DelegationRequest(String vers, String motif) {
    }

    public record TacheAssigneeRequest(Long trousseId, String titre, String description, String assigneA, String priorite,
                                       LocalDate echeance) {
    }

    /** {@code action} : DEMARRER, TERMINER, ROUVRIR ou ANNULER. */
    public record ActionTacheRequest(String action) {
    }

    /** Critères de la liste des trousses (UI-02) ; tous facultatifs. */
    public record CritereListe(String q, String statut, String initiateur, String type, LocalDate du, LocalDate au) {
    }

    public record VerificationRequest(String reponse) {
    }

    public record SignatureRequest(String nomComplet, boolean consentement, Map<String, String> champs) {
    }

    public record RefusRequest(String motif) {
    }

    // --- Vues ---
    public record TrousseResume(Long id, String reference, String titre, String statut, String source, String sfDossierId,
                                String unite, String proprietaire, LocalDate echeance, String typeContrat, Instant majLe,
                                String prochaineAction, boolean enRetard) {
    }

    public record DocumentVue(Long id, String cleDocument, int version, String nom, String type, long taille, String empreinte,
                              String statut, String analyse, String auteur, String motif, Instant creeLe) {
    }

    public record ParticipantVue(Long id, String nom, String courriel, String telephone, String roleContrat, boolean externe,
                                 String niveauIdentite, boolean questionDefinie) {
    }

    public record DecisionVue(String acteur, String nature, String commentaire, Instant decideLe) {
    }

    public record EtapeVue(Long id, String cle, String libelle, int phase, String type, List<AffectationDto> affectations,
                           int quorum, String politique, Integer delaiJours, List<Modele.Condition> conditions, String statut,
                           Instant activeeLe, Instant echeance, boolean enRetard, List<DecisionVue> decisions) {
    }

    public record InvitationVue(Long id, Long participantId, String participant, Long etapeId, String statut, String niveau,
                                Instant expireLe, Instant verifieeLe, Instant consentementLe, Instant signeeLe, String motif) {
    }

    public record TrousseDetail(TrousseResume resume, String risque, Long modeleId, Map<String, Preremplissage.Valeur> valeurs,
                                List<Modele.Champ> champs, List<String> manquants,
                                int rappelFrequenceJours, int rappelsMax, String copies, Instant sfRecupereLe,
                                List<DocumentVue> documents, List<ParticipantVue> participants, List<EtapeVue> etapes,
                                List<InvitationVue> invitations, Set<String> actions, List<String> alertes) {
    }

    public record EvenementVue(long sequence, String type, String acteur, Instant horodatage, String detail, String empreinte) {
    }

    public record TacheVue(Long etapeId, Long trousseId, String reference, String titre, String type, String libelle,
                           String demandeur, Instant echeance, boolean enRetard, String statut, boolean decidee,
                           String maDecision, boolean reassignee) {
    }

    public record TacheAssigneeVue(Long id, Long trousseId, String reference, String titre, String description,
                                   String assigneA, String priorite, LocalDate echeance, boolean enRetard, String statut,
                                   String creePar, Instant creeLe, Instant termineeLe, boolean peutDemarrer,
                                   boolean peutAnnuler) {
    }

    public record MembreVue(String courriel, String nom) {
    }

    public record Correspondance(String champ, String libelle, String source, Object valeur, String provenance, Instant recupereLe) {
    }

    public record PreremplissageVue(String sfDossierId, String numero, Map<String, Object> dossier, Instant recupereLe,
                                    List<Correspondance> correspondances, List<String> manquants,
                                    List<Preremplissage.Conflit> conflits) {
    }

    public record ModeleVue(Long id, String cleModele, int numero, String nom, String categorie, String statut,
                            String proprietaire, String groupes, String corps, List<Modele.Champ> champs,
                            List<Modele.Clause> clauses, List<EtapeRequest> parcours, Instant majLe, long verrou,
                            long trousses) {
    }

    public record Validation(boolean valide, List<String> erreurs) {
    }

    public record ApercuModele(String texte, List<String> clausesRetenues, List<String> champsMasques, List<String> erreurs) {
    }

    public record SuiviLigne(Long id, String reference, String titre, String statut, String source, String unite,
                             String proprietaire, List<String> etapesActives, List<String> enAttenteDe, Instant echeance,
                             boolean enRetard, String dernierEvenement, Instant dernierEvenementLe, List<String> exceptions) {
    }

    public record TableauBord(Map<String, Long> indicateurs, List<TacheVue> mesActions, List<EvenementVue> activite,
                              List<SortieVue> alertesSalesforce) {
    }

    public record SortieVue(Long id, Long trousseId, String sfDossierId, String evenement, String correlationId, String statut,
                            int tentatives, Instant prochainEssai, String derniereErreur, Instant creeLe, Instant envoyeLe) {
    }

    public record IntegrationVue(PortSalesforceEtat connexion, long enAttente, long enEchec, long envoyes, List<SortieVue> journal,
                                 ConfigIntegration config) {
    }

    /**
     * Connexion Salesforce de l'organisation, ou celle de la plateforme tant qu'elle n'en a pas ({@code personnalisee}
     * faux). Identifiants techniques toujours masqués : l'interface n'affiche jamais un secret.
     */
    public record ConfigIntegration(String mode, String instanceUrl, String tokenUrl, String objet, String versionApi,
                                    String clientIdMasque, boolean secretConfigure, String champStatut, String champReference,
                                    String champCorrelation, int reprisesMax, boolean personnalisee, String modifiePar,
                                    Instant modifieLe, List<String> hotesAutorises) {
    }

    /** Connexion propre à l'organisation ; un secret vide conserve celui en place, un secret saisi le remplace (rotation). */
    public record ConnexionRequest(String mode, String instanceUrl, String tokenUrl, String clientId, String clientSecret,
                                   String objet, String versionApi, String champStatut, String champReference,
                                   String champCorrelation, Integer reprisesMax) {
    }

    public record PortSalesforceEtat(boolean disponible, String mode, String detail, Instant testeLe) {
    }

    public record ChampPortail(String cle, String libelle, String type, boolean obligatoire, List<String> options,
                               Modele.Position position) {
    }

    public record DocumentPortail(Long id, String nom, String type, long taille, String empreinte) {
    }

    public record PortailVue(String reference, String titre, String participant, String niveau, String statut,
                             Instant expireLe, boolean verifiee, boolean consentement, String question,
                             List<DocumentPortail> documents, List<ChampPortail> champs, Recu recu, String organisation, String logo) {
    }

    public record Recu(String reference, String signataire, Instant signeeLe, String empreinteDocuments, String attestation) {
    }

    public record SignatureVue(String participant, String courriel, String roleContrat, String niveau, Instant verifieeLe,
                               Instant consentementLe, Instant signeeLe, String ip, String statut, Map<String, Object> attestation) {
    }

    public record Integrite(boolean integre, boolean journalIntegre, Long rupture, List<String> documentsAlteres,
                            List<String> attestationsInvalides, Instant verifieeLe) {
    }

    public record PreuveVue(TrousseResume trousse, List<DocumentVue> documentsFinaux, List<SignatureVue> signatures,
                            List<EvenementVue> evenements, Integrite integrite) {
    }

    public record AnalyseVue(Long id, Long trousseId, Long documentId, String statut, Map<String, Object> resultat, String lanceePar,
                             String reviseePar, String justification, Instant creeLe, Instant reviseeLe) {
    }

    public record MessageVue(Long id, String evenement, String canal, String sujet, String corps, boolean actif) {
    }

    /** UI-22 : réglages de la plateforme (lecture seule) et paramètres du module que l'organisation modifie. */
    public record ParametresVue(String environnement, String salesforceMode, boolean planificateurActif, long planificateurDelaiMs,
                                List<String> formats, long sessionMinutes, boolean cookieSecurise, Instant demarreLe,
                                ParametresModule module) {
    }

    /** Niveau d'identité minimal par risque. */
    public record NiveauxIdentite(String faible, String moyen, String eleve) {
    }

    /** Paramètres effectifs du module ; {@code personnalise} faux : l'organisation suit ceux de la plateforme. */
    public record ParametresModule(long tailleMaxOctets, long plafondOctets, int invitationValiditeJours, String portailUrl,
                                   String prefixeReference, int rappelFrequenceJours, int rappelsMax, int preavisEcheanceJours,
                                   NiveauxIdentite identiteExterne, NiveauxIdentite identiteInterne, int tentativesIdentite,
                                   int codeValiditeMinutes, String champMontant, boolean personnalise, String modifiePar,
                                   Instant modifieLe) {
    }

    /** Modification partielle : un paramètre absent garde sa valeur effective. */
    public record ParametresModuleRequest(Long tailleMaxOctets, Integer invitationValiditeJours, String portailUrl,
                                          String prefixeReference, Integer rappelFrequenceJours, Integer rappelsMax,
                                          Integer preavisEcheanceJours, NiveauxIdentite identiteExterne, NiveauxIdentite identiteInterne,
                                          Integer tentativesIdentite, Integer codeValiditeMinutes, String champMontant) {
    }

    /** Paramètres dont tout utilisateur du module a besoin : dépôt, rappels par défaut, identité exigée par risque. */
    public record ParametresPublics(long tailleMaxOctets, List<String> formats, int rappelFrequenceJours, int rappelsMax,
                                    NiveauxIdentite identiteExterne, NiveauxIdentite identiteInterne) {
    }

    /** Ligne du journal d'audit CLM (UI-23) ; {@code systeme} distingue les événements automatiques des actions métier. */
    public record JournalLigne(Long trousseId, String reference, long sequence, String type, String acteur, Instant horodatage,
                               String detail, boolean systeme, String empreinte) {
    }

    // --- Intégration entrante : Salesforce, Workday, autres systèmes (addenda 1 : TI-22, AF-47) ---
    public record ClientIntegrationRequest(String nom, String systeme, String urlRappel) {
    }

    public record ClientIntegrationVue(Long id, String nom, String systeme, String clientId, String urlRappel, boolean actif,
                                       String creePar, Instant creeLe, Instant revoqueLe) {
    }

    /** Secrets montrés une seule fois, à la création : la plateforme n'en garde que l'empreinte ou la forme chiffrée. */
    public record ClientIntegrationCree(ClientIntegrationVue client, String secret, String cleRappel) {
    }

    /** Réponse OAuth 2.0 (RFC 6749, § 5.1) : les noms de champs suivent la norme, pas la convention Java. */
    public record JetonAcces(String access_token, String token_type, long expires_in) {
    }

    public record DocumentJoint(String nom, String contenuBase64) {
    }

    /** Trousse demandée par un système externe ; valeurs, documents et signataires sont facultatifs avec un modèle. */
    public record DemandeTrousse(String titre, String referenceExterne, Long modeleId, String sfDossierId, String initiateur,
                                 String unite, String risque, LocalDate echeance, Map<String, Object> valeurs,
                                 List<DocumentJoint> documents, List<ParticipantRequest> signataires, Boolean lancer) {
    }

    /** Lot : les valeurs du lot (modèle, initiateur, lancement) valent pour chaque élément qui ne les précise pas. */
    public record DemandeLot(String libelle, Long modeleId, String initiateur, Boolean lancer, List<DemandeTrousse> elements) {
    }

    public record TrousseExterneVue(Long id, String reference, String statut, String source, String referenceExterne,
                                    String sfDossierId, Long lotId, List<DocumentVue> documents) {
    }

    public record ElementLotVue(int rang, String referenceExterne, Long trousseId, String reference, String statut, String erreur) {
    }

    public record LotVue(Long id, String libelle, Long modeleId, String origine, String statut, int total, int reussis,
                         int echecs, boolean lancer, String creePar, Instant creeLe, List<ElementLotVue> elements) {
    }

    public record RappelVue(Long id, String client, Long trousseId, String evenement, String statut, int tentatives,
                            String derniereErreur, Instant creeLe, Instant envoyeLe) {
    }
}
