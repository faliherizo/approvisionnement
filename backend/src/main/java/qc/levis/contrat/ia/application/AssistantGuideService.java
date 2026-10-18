package qc.levis.contrat.ia.application;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.audit.application.AuditService;
import qc.levis.contrat.commun.domain.Enums;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.dossier.application.DossierService;
import qc.levis.contrat.ia.application.dto.AssistantContexteRequest;
import qc.levis.contrat.ia.application.dto.AssistantGenerationRequest;
import qc.levis.contrat.ia.application.dto.AssistantSuggestion;
import qc.levis.contrat.ia.application.dto.ClauseApercu;
import qc.levis.contrat.ia.application.dto.TemplateRecommande;
import qc.levis.contrat.redaction.application.DocumentService;
import qc.levis.contrat.redaction.application.RuleEngine;
import qc.levis.contrat.redaction.application.TemplateService;
import qc.levis.contrat.redaction.application.dto.DocumentRequest;
import qc.levis.contrat.redaction.application.dto.InstancierTemplateRequest;
import qc.levis.contrat.redaction.application.repository.ClauseRepository;
import qc.levis.contrat.redaction.application.repository.DocumentTemplateRepository;
import qc.levis.contrat.redaction.domain.Clause;
import qc.levis.contrat.redaction.domain.Document;
import qc.levis.contrat.redaction.domain.DocumentTemplate;

/**
 * Assistant guidé de génération (art. 7.2). Couche d'orchestration au-dessus du
 * {@link RuleEngine}, du {@link TemplateService} et du {@link DocumentService} : il
 * <em>ne réimplémente aucun moteur</em> (ADR-004). Il recommande un mode de sollicitation
 * et des templates, prévisualise les clauses éligibles, puis produit un document.
 */
@Service
public class AssistantGuideService {

    private final DocumentTemplateRepository templates;
    private final ClauseRepository clauses;
    private final RuleEngine ruleEngine;
    private final TemplateService templateService;
    private final DocumentService documentService;
    private final DossierService dossierService;
    private final AuditService audit;

    public AssistantGuideService(DocumentTemplateRepository templates, ClauseRepository clauses,
                                 RuleEngine ruleEngine, TemplateService templateService,
                                 DocumentService documentService, DossierService dossierService,
                                 AuditService audit) {
        this.templates = templates;
        this.clauses = clauses;
        this.ruleEngine = ruleEngine;
        this.templateService = templateService;
        this.documentService = documentService;
        this.dossierService = dossierService;
        this.audit = audit;
    }

    /** Analyse le contexte saisi et renvoie des recommandations (sans rien persister). */
    public AssistantSuggestion analyser(AssistantContexteRequest req) {
        Enums.ModeSollicitation modeSuggere = ruleEngine.modeSuggere(req.valeurEstimee());

        Document contexteTransitoire = new Document();
        contexteTransitoire.setType(req.type());
        contexteTransitoire.setModeSollicitation(modeSuggere);
        contexteTransitoire.setValeurEstimee(req.valeurEstimee());
        contexteTransitoire.setContexte(req.contexte());

        List<ClauseApercu> eligibles = new ArrayList<>();
        for (Clause c : clauses.findByActifTrueAndTenantId(TenantContext.get())) {
            if (ruleEngine.estEligible(c, contexteTransitoire)) {
                eligibles.add(new ClauseApercu(c.getId(), c.getCode(), c.getTitre(),
                        c.getCategorie().name(), c.isNormative()));
            }
        }

        List<TemplateRecommande> recommandes = new ArrayList<>();
        for (DocumentTemplate t : templates.findByTenantId(TenantContext.get())) {
            boolean memeType = req.type() != null && req.type() == t.getType();
            boolean memeCategorie = req.categorieAcquisition() != null
                    && req.categorieAcquisition() == t.getCategorieAcquisition();
            if (memeType || memeCategorie) {
                recommandes.add(new TemplateRecommande(t.getId(), t.getCode(), t.getNom(),
                        t.getType(), t.getCategorieAcquisition(), t.getClauseCodes().size(),
                        memeType && memeCategorie));
            }
        }
        recommandes.sort(Comparator.comparing(TemplateRecommande::correspondanceExacte).reversed());

        return new AssistantSuggestion(modeSuggere, recommandes, eligibles);
    }

    /**
     * Produit un document guidé : soit par instanciation d'un template, soit par création
     * puis assemblage d'une sélection manuelle. Rattache au dossier si demandé.
     */
    @Transactional
    public Document generer(AssistantGenerationRequest req, String utilisateur) {
        Document document = req.templateId() != null
                ? genererDepuisTemplate(req, utilisateur)
                : genererManuellement(req, utilisateur);
        audit.log(utilisateur, "ASSISTANT_GENERER", "Document", document.getId(),
                req.templateId() != null ? "template=" + req.templateId() : "manuel");
        return document;
    }

    private Document genererDepuisTemplate(AssistantGenerationRequest req, String utilisateur) {
        Document document = templateService.instancier(req.templateId(),
                new InstancierTemplateRequest(req.titre(), req.valeurEstimee(), req.contexte()),
                utilisateur);
        if (req.dossierId() != null) {
            document = dossierService.rattacher(req.dossierId(), document.getId(), utilisateur);
        }
        return document;
    }

    private Document genererManuellement(AssistantGenerationRequest req, String utilisateur) {
        Enums.ModeSollicitation mode = req.modeSollicitation() != null
                ? req.modeSollicitation()
                : ruleEngine.modeSuggere(req.valeurEstimee());
        Document cree = documentService.creer(new DocumentRequest(
                req.titre(), req.type(), mode, req.valeurEstimee(), req.contexte(),
                null, null, null, null, req.dossierId()), utilisateur);
        return documentService.assembler(cree.getId(), req.clauseIds(), utilisateur);
    }
}
