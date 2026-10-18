package qc.levis.contrat.redaction.application;

import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.audit.application.AuditService;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.redaction.application.dto.AnnexeRequest;
import qc.levis.contrat.redaction.application.dto.DocumentRequest;
import qc.levis.contrat.redaction.application.dto.InstancierTemplateRequest;
import qc.levis.contrat.redaction.application.dto.TemplateRequest;
import qc.levis.contrat.redaction.application.repository.ClauseRepository;
import qc.levis.contrat.redaction.application.repository.DocumentTemplateRepository;
import qc.levis.contrat.redaction.domain.Clause;
import qc.levis.contrat.redaction.domain.Document;
import qc.levis.contrat.redaction.domain.DocumentTemplate;

/** Templates de documents nommés + instanciation — écart CLM E-6. */
@Service
public class TemplateService {

    private final DocumentTemplateRepository templates;
    private final ClauseRepository clauses;
    private final DocumentService documents;
    private final AnnexeService annexes;
    private final AuditService audit;

    public TemplateService(DocumentTemplateRepository templates, ClauseRepository clauses,
                           DocumentService documents, AnnexeService annexes, AuditService audit) {
        this.templates = templates;
        this.clauses = clauses;
        this.documents = documents;
        this.annexes = annexes;
        this.audit = audit;
    }

    public List<DocumentTemplate> lister() {
        return templates.findByTenantId(TenantContext.get());
    }

    public DocumentTemplate obtenir(Long id) {
        return templates.findById(id)
                .filter(t -> java.util.Objects.equals(t.getTenantId(), TenantContext.get()))
                .orElseThrow(() -> new NoSuchElementException("Template introuvable: " + id));
    }

    @Transactional
    public DocumentTemplate creer(TemplateRequest req) {
        DocumentTemplate t = new DocumentTemplate();
        t.setTenantId(TenantContext.get());
        appliquer(t, req);
        return templates.save(t);
    }

    @Transactional
    public DocumentTemplate modifier(Long id, TemplateRequest req) {
        DocumentTemplate t = obtenir(id);
        appliquer(t, req);
        return templates.save(t);
    }

    @Transactional
    public void supprimer(Long id) {
        templates.delete(obtenir(id));
    }

    private void appliquer(DocumentTemplate t, TemplateRequest req) {
        t.setCode(req.code());
        t.setNom(req.nom());
        t.setType(req.type());
        t.setModeSollicitation(req.modeSollicitation());
        t.setCategorieAcquisition(req.categorieAcquisition());
        t.setDescription(req.description());
        t.setTitreParDefaut(req.titreParDefaut());
        t.setContexte(req.contexte());
        t.setGabaritCode(req.gabaritCode());
        t.setClauseCodes(req.clauseCodes() != null ? new java.util.ArrayList<>(req.clauseCodes()) : new java.util.ArrayList<>());
        t.setAnnexes(req.annexes() != null ? new java.util.ArrayList<>(req.annexes()) : new java.util.ArrayList<>());
    }

    /**
     * Crée un document à partir d'un template : pré-remplit titre/contexte, applique le gabarit
     * de design, assemble les clauses prévues et crée les annexes par défaut.
     */
    @Transactional
    public Document instancier(Long templateId, InstancierTemplateRequest req, String utilisateur) {
        DocumentTemplate t = templates.findById(templateId)
                .orElseThrow(() -> new NoSuchElementException("Template introuvable: " + templateId));
        Long tenant = TenantContext.get();
        if (tenant != null && !tenant.equals(t.getTenantId())) {
            throw new NoSuchElementException("Template introuvable: " + templateId);
        }

        String titre = notBlank(req.titre()) ? req.titre() : t.getTitreParDefaut();
        String contexte = notBlank(req.contexte()) ? req.contexte() : t.getContexte();
        Document doc = documents.creer(new DocumentRequest(
                notBlank(titre) ? titre : t.getNom(), t.getType(), t.getModeSollicitation(),
                req.valeurEstimee(), contexte, null, null, null, t.getGabaritCode(), null), utilisateur);

        // Résout les codes de clauses du template (scopées au tenant) en identifiants.
        List<Long> ids = t.getClauseCodes().stream()
                .map(code -> clauses.findByCodeAndTenantId(code, tenant).map(Clause::getId).orElse(null))
                .filter(java.util.Objects::nonNull)
                .toList();
        Document assemble = documents.assembler(doc.getId(), ids, utilisateur);

        // Annexes par défaut du template.
        for (String titreAnnexe : t.getAnnexes()) {
            if (notBlank(titreAnnexe)) {
                annexes.ajouter(doc.getId(), new AnnexeRequest(null, titreAnnexe, null, null), utilisateur);
            }
        }

        audit.log(utilisateur, "INSTANCIER_TEMPLATE", "DocumentTemplate", templateId,
                "-> Document " + doc.getId());
        return assemble;
    }

    private static boolean notBlank(String s) { return s != null && !s.isBlank(); }
}
