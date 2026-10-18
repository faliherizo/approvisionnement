package qc.levis.contrat.redaction.application;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.approbation.application.repository.ApprovalStepRepository;
import qc.levis.contrat.approbation.application.repository.ApprovalWorkflowRepository;
import qc.levis.contrat.approbation.domain.ApprovalStep;
import qc.levis.contrat.audit.application.AuditService;
import qc.levis.contrat.commun.domain.Enums;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.redaction.application.dto.ClauseDisponible;
import qc.levis.contrat.redaction.application.dto.CommentRequest;
import qc.levis.contrat.redaction.application.dto.DocumentClauseRequest;
import qc.levis.contrat.redaction.application.dto.DocumentRequest;
import qc.levis.contrat.redaction.application.repository.ClauseRepository;
import qc.levis.contrat.redaction.application.repository.CommentRepository;
import qc.levis.contrat.redaction.application.repository.DocumentRepository;
import qc.levis.contrat.redaction.domain.Clause;
import qc.levis.contrat.redaction.domain.Comment;
import qc.levis.contrat.redaction.domain.Document;
import qc.levis.contrat.redaction.domain.DocumentClause;

/** Orchestration du cycle de vie d'un document contractuel. */
@Service
public class DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);

    private final DocumentRepository documents;
    private final ClauseRepository clauses;
    private final CommentRepository comments;
    private final ApprovalStepRepository approvals;
    private final ApprovalWorkflowRepository workflows;
    private final RuleEngine ruleEngine;
    private final AuditService audit;

    /** Circuit par défaut si aucun gabarit de workflow n'est configuré pour le type. */
    private static final Enums.Role[] CIRCUIT_DEFAUT =
            {Enums.Role.APPRO, Enums.Role.JURIDIQUE, Enums.Role.DIRECTION};

    public DocumentService(DocumentRepository documents, ClauseRepository clauses,
                           CommentRepository comments, ApprovalStepRepository approvals,
                           ApprovalWorkflowRepository workflows,
                           RuleEngine ruleEngine, AuditService audit) {
        this.documents = documents;
        this.clauses = clauses;
        this.comments = comments;
        this.approvals = approvals;
        this.workflows = workflows;
        this.ruleEngine = ruleEngine;
        this.audit = audit;
    }

    /** Documents actifs du tenant courant (isolation multi-tenant ; exclut les supprimés). */
    public List<Document> lister() {
        return documents.findByTenantIdAndActifTrue(TenantContext.get());
    }

    /** Suppression logique d'un document (masqué des listes, conservé pour l'audit). */
    @Transactional
    public void supprimer(Long id, String utilisateur) {
        Document d = get(id);
        d.setActif(false);
        documents.save(d);
        audit.log(utilisateur, "SUPPRIMER", "Document", id, d.getTitre());
    }

    /** Charge un document en vérifiant qu'il appartient au tenant courant. */
    public Document get(Long id) {
        Document d = documents.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Document introuvable: " + id));
        Long tenant = TenantContext.get();
        if (tenant != null && !tenant.equals(d.getTenantId())) {
            // N'expose pas les documents d'un autre tenant.
            throw new NoSuchElementException("Document introuvable: " + id);
        }
        return d;
    }

    @Transactional
    public Document creer(DocumentRequest req, String utilisateur) {
        Document d = new Document();
        d.setTitre(req.titre());
        d.setType(req.type());
        d.setModeSollicitation(req.modeSollicitation());
        d.setValeurEstimee(req.valeurEstimee());
        d.setContexte(req.contexte());
        d.setNumeroAO(req.numeroAO());
        d.setOrganismePublic(req.organismePublic());
        d.setPrestataire(req.prestataire());
        d.setGabaritCode(req.gabaritCode());
        d.setDossierId(req.dossierId());
        d.setCreePar(utilisateur);
        d.setTenantId(TenantContext.get());
        d.setStatut(Enums.StatutDocument.BROUILLON);
        Document saved = documents.save(d);
        audit.log(utilisateur, "CREER", "Document", saved.getId(), "Type=" + req.type());
        log.info("Document {} créé (type {}, tenant {})", saved.getId(), req.type(), saved.getTenantId());
        return saved;
    }

    /** Clauses actives du tenant courant. */
    private List<Clause> clausesActives() {
        return clauses.findByActifTrueAndTenantId(TenantContext.get());
    }

    /** Clauses éligibles selon le moteur de règles pour le contexte du document. */
    public List<Clause> clausesEligibles(Long documentId) {
        Document d = get(documentId);
        List<Clause> result = new ArrayList<>();
        for (Clause c : clausesActives()) {
            if (ruleEngine.estEligible(c, d)) {
                result.add(c);
            }
        }
        return result;
    }

    /**
     * Toutes les clauses actives proposées à l'assemblage, annotées du verdict du
     * moteur de règles. Le rédacteur voit ainsi l'ensemble du catalogue (et peut
     * forcer l'inclusion d'une clause), pas seulement les clauses éligibles.
     */
    public List<ClauseDisponible> clausesDisponibles(Long documentId) {
        Document d = get(documentId);
        List<ClauseDisponible> result = new ArrayList<>();
        for (Clause c : clausesActives()) {
            boolean eligible = ruleEngine.estEligible(c, d);
            boolean autoInclus = eligible && c.getCategorie() == Enums.ClauseCategorie.STANDARD;
            result.add(new ClauseDisponible(c.getId(), c.getCode(), c.getTitre(),
                    c.getCategorie().name(), c.isNormative(), eligible, autoInclus));
        }
        return result;
    }

    /**
     * Assemble le document : intègre les clauses standards éligibles, plus les
     * clauses optionnelles/personnalisées explicitement sélectionnées.
     */
    @Transactional
    public Document assembler(Long documentId, List<Long> clauseIdsSelectionnees, String utilisateur) {
        Document d = get(documentId);
        d.getClauses().clear();

        List<Clause> actives = clausesActives();
        java.util.Map<String, Clause> parCode = new java.util.LinkedHashMap<>();
        actives.forEach(c -> parCode.put(c.getCode(), c));

        // 1) Candidats : clauses STANDARD éligibles (auto) + toute clause explicitement
        //    sélectionnée par le rédacteur (la sélection manuelle prime sur le moteur).
        java.util.LinkedHashSet<Clause> retenues = new java.util.LinkedHashSet<>();
        for (Clause c : actives) {
            boolean eligible = ruleEngine.estEligible(c, d);
            boolean selectionnee = clauseIdsSelectionnees != null
                    && clauseIdsSelectionnees.contains(c.getId());
            boolean autoInclus = eligible && c.getCategorie() == Enums.ClauseCategorie.STANDARD;
            if (autoInclus || selectionnee) {
                retenues.add(c);
            }
        }

        // 2) Dépendances : auto-inclusion des clauses requises (si présentes et éligibles).
        java.util.Deque<Clause> aTraiter = new java.util.ArrayDeque<>(retenues);
        while (!aTraiter.isEmpty()) {
            for (String codeRequis : aTraiter.poll().getDependances()) {
                Clause req = parCode.get(codeRequis);
                if (req != null && ruleEngine.estEligible(req, d) && retenues.add(req)) {
                    aTraiter.add(req);
                }
            }
        }

        // 3) Exclusions : on retire toute clause exclue par une clause conservée (priorité à
        //    la première rencontrée), pour éviter des combinaisons incompatibles.
        java.util.LinkedHashSet<Clause> finales = new java.util.LinkedHashSet<>();
        java.util.Set<String> exclus = new java.util.HashSet<>();
        for (Clause c : retenues) {
            if (exclus.contains(c.getCode())) continue;
            finales.add(c);
            exclus.addAll(c.getExclusions());
        }

        // 4) Matérialisation ordonnée.
        int ordre = 1;
        for (Clause c : finales) {
            DocumentClause dc = new DocumentClause();
            dc.setDocument(d);
            dc.setClauseId(c.getId());
            dc.setCode(c.getCode());
            dc.setNumero(c.getNumero());
            dc.setSection(c.getSection());
            dc.setTitre(c.getTitre());
            dc.setTexteEffectif(c.getCorps());
            dc.setNormative(c.isNormative());
            dc.setOrdre(ordre++);
            d.getClauses().add(dc);
        }
        Document saved = documents.save(d);
        audit.log(utilisateur, "ASSEMBLER", "Document", documentId, d.getClauses().size() + " clauses");
        return saved;
    }

    // --- Rédaction locale du contenu des sections (surcouche au catalogue) ---

    /**
     * Adapte localement le texte (ou le titre/section/ordre) d'une section du document,
     * sans toucher la clause du catalogue. Les clauses normatives sont verrouillées.
     */
    @Transactional
    public Document modifierClause(Long documentId, Long clauseId, DocumentClauseRequest req, String utilisateur) {
        Document d = get(documentId);
        DocumentClause dc = clauseDu(d, clauseId);
        if (dc.isNormative()) {
            throw new IllegalStateException("Clause normative verrouillée : elle ne peut pas être modifiée dans le document.");
        }
        if (req.titre() != null) dc.setTitre(req.titre());
        if (req.section() != null) dc.setSection(req.section());
        if (req.numero() != null) dc.setNumero(req.numero());
        if (req.texteEffectif() != null) dc.setTexteEffectif(req.texteEffectif());
        if (req.ordre() != null) dc.setOrdre(req.ordre());
        dc.setModifieLocalement(true);
        Document saved = documents.save(d);
        audit.log(utilisateur, "REDIGER_SECTION", "Document", documentId, dc.getCode());
        return saved;
    }

    /** Ajoute une section rédigée librement (hors catalogue), rendue comme les autres. */
    @Transactional
    public Document ajouterClauseLibre(Long documentId, DocumentClauseRequest req, String utilisateur) {
        Document d = get(documentId);
        DocumentClause dc = new DocumentClause();
        dc.setDocument(d);
        dc.setClauseId(null);
        dc.setCode("LIBRE");
        dc.setTitre(req.titre() != null && !req.titre().isBlank() ? req.titre() : "Nouvelle section");
        dc.setSection(req.section() != null && !req.section().isBlank() ? req.section() : "Clauses particulières");
        dc.setNumero(req.numero());
        dc.setTexteEffectif(req.texteEffectif() != null ? req.texteEffectif() : "");
        dc.setNormative(false);
        dc.setModifieLocalement(true);
        dc.setOrdre(req.ordre() != null ? req.ordre() : prochainOrdre(d));
        d.getClauses().add(dc);
        Document saved = documents.save(d);
        audit.log(utilisateur, "SECTION_LIBRE", "Document", documentId, dc.getTitre());
        return saved;
    }

    /** Retire une section du document (clause libre ou clause non normative). */
    @Transactional
    public Document supprimerClause(Long documentId, Long clauseId, String utilisateur) {
        Document d = get(documentId);
        DocumentClause dc = clauseDu(d, clauseId);
        if (dc.isNormative()) {
            throw new IllegalStateException("Clause normative obligatoire : elle ne peut pas être retirée du document.");
        }
        d.getClauses().remove(dc);
        Document saved = documents.save(d);
        audit.log(utilisateur, "RETIRER_SECTION", "Document", documentId, dc.getCode());
        return saved;
    }

    private DocumentClause clauseDu(Document d, Long clauseId) {
        return d.getClauses().stream().filter(c -> clauseId.equals(c.getId())).findFirst()
                .orElseThrow(() -> new NoSuchElementException("Section introuvable dans le document: " + clauseId));
    }

    private int prochainOrdre(Document d) {
        return d.getClauses().stream().mapToInt(DocumentClause::getOrdre).max().orElse(0) + 1;
    }

    // --- Circuit d'approbation hiérarchisé ---

    @Transactional
    public List<ApprovalStep> demarrerApprobation(Long documentId, String utilisateur) {
        Document d = get(documentId);
        d.setStatut(Enums.StatutDocument.EN_APPROBATION);
        documents.save(d);
        // Circuit configurable par type (gabarit de workflow), sinon circuit par défaut.
        List<Enums.Role> circuit = circuitPour(d);
        List<ApprovalStep> steps = new ArrayList<>();
        int niveau = 1;
        for (Enums.Role r : circuit) {
            ApprovalStep s = new ApprovalStep();
            s.setDocumentId(documentId);
            s.setNiveau(niveau++);
            s.setRoleRequis(r);
            steps.add(approvals.save(s));
        }
        audit.log(utilisateur, "DEMARRER_APPROBATION", "Document", documentId, null);
        return steps;
    }

    /** Résout le circuit d'approbation : gabarit configuré pour le type, ou circuit par défaut. */
    private List<Enums.Role> circuitPour(Document d) {
        return workflows.findFirstByTenantIdAndTypeAndActifTrue(d.getTenantId(), d.getType())
                .filter(w -> !w.getEtapes().isEmpty())
                .map(w -> new ArrayList<>(w.getEtapes()))
                .map(l -> (List<Enums.Role>) l)
                .orElseGet(() -> List.of(CIRCUIT_DEFAUT));
    }

    public List<ApprovalStep> etapesApprobation(Long documentId) {
        get(documentId); // vérifie l'appartenance au tenant
        return approvals.findByDocumentIdOrderByNiveauAsc(documentId);
    }

    @Transactional
    public ApprovalStep decider(Long stepId, boolean approuve, String note, String utilisateur) {
        ApprovalStep s = approvals.findById(stepId)
                .orElseThrow(() -> new NoSuchElementException("Étape introuvable: " + stepId));
        s.setStatut(approuve ? Enums.StatutApprobation.APPROUVE : Enums.StatutApprobation.REFUSE);
        s.setApprobateur(utilisateur);
        s.setNote(note);
        s.setDecideLe(Instant.now());
        approvals.save(s);

        Document d = get(s.getDocumentId());
        if (!approuve) {
            d.setStatut(Enums.StatutDocument.REFUSE);
        } else {
            boolean toutApprouve = approvals.findByDocumentIdOrderByNiveauAsc(s.getDocumentId())
                    .stream().allMatch(e -> e.getStatut() == Enums.StatutApprobation.APPROUVE);
            if (toutApprouve) {
                d.setStatut(Enums.StatutDocument.APPROUVE);
            }
        }
        documents.save(d);
        audit.log(utilisateur, approuve ? "APPROUVER" : "REFUSER", "ApprovalStep", stepId, note);
        log.info("Étape {} du document {} {} → statut du document : {}",
                stepId, s.getDocumentId(), approuve ? "approuvée" : "refusée", d.getStatut());
        return s;
    }

    // --- Commentaires ---

    public List<Comment> commentaires(Long documentId) {
        get(documentId); // vérifie l'appartenance au tenant
        return comments.findByDocumentIdOrderByCreeLeAsc(documentId);
    }

    @Transactional
    public Comment commenter(Long documentId, CommentRequest req, String utilisateur) {
        get(documentId); // valide l'existence
        Comment c = new Comment();
        c.setDocumentId(documentId);
        c.setClauseId(req.clauseId());
        c.setParentId(req.parentId());
        c.setAuteur(utilisateur);
        c.setCorps(req.corps());
        Comment saved = comments.save(c);
        audit.log(utilisateur, "COMMENTER", "Document", documentId, null);
        return saved;
    }

    /** Marque un commentaire comme résolu (ou le rouvre) — suivi des contributions (art. 7.5). */
    @Transactional
    public Comment resoudreCommentaire(Long documentId, Long commentaireId, boolean resolu, String utilisateur) {
        get(documentId); // vérifie l'appartenance au tenant
        Comment c = comments.findById(commentaireId)
                .filter(x -> documentId.equals(x.getDocumentId()))
                .orElseThrow(() -> new NoSuchElementException("Commentaire introuvable: " + commentaireId));
        c.setResolu(resolu);
        c.setResoluPar(resolu ? utilisateur : null);
        Comment saved = comments.save(c);
        audit.log(utilisateur, resolu ? "RESOUDRE_COMMENTAIRE" : "ROUVRIR_COMMENTAIRE",
                "Document", documentId, "commentaire=" + commentaireId);
        return saved;
    }
}
