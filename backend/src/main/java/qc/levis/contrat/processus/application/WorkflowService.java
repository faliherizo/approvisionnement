package qc.levis.contrat.processus.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.processus.application.repository.WorkflowRepository;
import qc.levis.contrat.processus.domain.Workflow;
import qc.levis.contrat.processus.domain.WorkflowDef;

/** Conception des workflows (définition étapes/conditions en JSON, un par formulaire). Tenant-scopé. */
@Service
public class WorkflowService {

    private static final String VIDE = "{\"etapes\":[]}";

    private final WorkflowRepository repo;
    private final ObjectMapper mapper = new ObjectMapper();

    public WorkflowService(WorkflowRepository repo) {
        this.repo = repo;
    }

    /** Définition JSON du workflow d'un formulaire (ou structure vide si aucun). */
    public String lire(Long formulaireId) {
        return repo.findByTenantIdAndFormulaireId(tenant(), formulaireId)
                .map(Workflow::getDefinitionJson).orElse(VIDE);
    }

    @Transactional
    public String enregistrer(Long formulaireId, String definitionJson) {
        Workflow w = repo.findByTenantIdAndFormulaireId(tenant(), formulaireId)
                .orElseGet(() -> new Workflow(tenant(), formulaireId, VIDE));
        w.setDefinitionJson(definitionJson != null ? definitionJson : VIDE);
        return repo.save(w).getDefinitionJson();
    }

    /** Parse la définition JSON en objet de domaine ; workflow vide si absent/illisible. */
    public WorkflowDef extraire(String definitionJson) {
        if (definitionJson == null || definitionJson.isBlank()) return new WorkflowDef(java.util.List.of());
        try {
            return mapper.readValue(definitionJson, WorkflowDef.class);
        } catch (Exception e) {
            return new WorkflowDef(java.util.List.of());
        }
    }

    private Long tenant() {
        Long t = TenantContext.get();
        if (t == null) throw new IllegalStateException("Opération réservée à un espace tenant.");
        return t;
    }
}
