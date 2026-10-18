package qc.levis.contrat.approbation.infrastructure.web;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import qc.levis.contrat.approbation.application.ApprovalWorkflowService;
import qc.levis.contrat.approbation.application.dto.WorkflowRequest;
import qc.levis.contrat.approbation.domain.ApprovalWorkflow;

/** Gabarits de circuit d'approbation configurables (art. 7.5, ADR-005). Réservé à l'administration. */
@RestController
@RequestMapping("/api/workflows")
@PreAuthorize("hasRole('ADMIN')")
public class ApprovalWorkflowController {

    private final ApprovalWorkflowService service;

    public ApprovalWorkflowController(ApprovalWorkflowService service) {
        this.service = service;
    }

    private String utilisateur(Authentication auth) {
        return auth != null ? auth.getName() : "anonyme";
    }

    @GetMapping
    public List<ApprovalWorkflow> lister() { return service.lister(); }

    @GetMapping("/{id}")
    public ApprovalWorkflow get(@PathVariable Long id) { return service.get(id); }

    @PostMapping
    public ApprovalWorkflow creer(@Valid @RequestBody WorkflowRequest req, Authentication auth) {
        return service.creer(req, utilisateur(auth));
    }

    @PutMapping("/{id}")
    public ApprovalWorkflow modifier(@PathVariable Long id, @Valid @RequestBody WorkflowRequest req,
                                     Authentication auth) {
        return service.modifier(id, req, utilisateur(auth));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id, Authentication auth) {
        service.supprimer(id, utilisateur(auth));
        return ResponseEntity.noContent().build();
    }
}
