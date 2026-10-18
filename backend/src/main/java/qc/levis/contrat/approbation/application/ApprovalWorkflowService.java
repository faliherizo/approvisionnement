package qc.levis.contrat.approbation.application;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.approbation.application.dto.WorkflowRequest;
import qc.levis.contrat.approbation.application.repository.ApprovalWorkflowRepository;
import qc.levis.contrat.approbation.domain.ApprovalWorkflow;
import qc.levis.contrat.audit.application.AuditService;
import qc.levis.contrat.commun.infrastructure.TenantContext;

/** Gestion des gabarits de circuit d'approbation configurables (art. 7.5, ADR-005). */
@Service
public class ApprovalWorkflowService {

    private final ApprovalWorkflowRepository workflows;
    private final AuditService audit;

    public ApprovalWorkflowService(ApprovalWorkflowRepository workflows, AuditService audit) {
        this.workflows = workflows;
        this.audit = audit;
    }

    public List<ApprovalWorkflow> lister() {
        return workflows.findByTenantId(TenantContext.get());
    }

    public ApprovalWorkflow get(Long id) {
        ApprovalWorkflow w = workflows.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Workflow introuvable: " + id));
        Long tenant = TenantContext.get();
        if (tenant != null && !tenant.equals(w.getTenantId())) {
            throw new NoSuchElementException("Workflow introuvable: " + id);
        }
        return w;
    }

    @Transactional
    public ApprovalWorkflow creer(WorkflowRequest req, String utilisateur) {
        ApprovalWorkflow w = new ApprovalWorkflow();
        w.setTenantId(TenantContext.get());
        appliquer(w, req);
        ApprovalWorkflow saved = workflows.save(w);
        audit.log(utilisateur, "CREER", "ApprovalWorkflow", saved.getId(), req.nom());
        return saved;
    }

    @Transactional
    public ApprovalWorkflow modifier(Long id, WorkflowRequest req, String utilisateur) {
        ApprovalWorkflow w = get(id);
        appliquer(w, req);
        ApprovalWorkflow saved = workflows.save(w);
        audit.log(utilisateur, "MODIFIER", "ApprovalWorkflow", id, req.nom());
        return saved;
    }

    @Transactional
    public void supprimer(Long id, String utilisateur) {
        ApprovalWorkflow w = get(id);
        workflows.delete(w);
        audit.log(utilisateur, "SUPPRIMER", "ApprovalWorkflow", id, w.getNom());
    }

    private void appliquer(ApprovalWorkflow w, WorkflowRequest req) {
        if (req.etapes().isEmpty()) {
            throw new IllegalArgumentException("Un circuit d'approbation exige au moins une étape.");
        }
        w.setNom(req.nom());
        w.setType(req.type());
        w.setActif(req.actif() == null || req.actif());
        w.setEtapes(new ArrayList<>(req.etapes()));
    }
}
