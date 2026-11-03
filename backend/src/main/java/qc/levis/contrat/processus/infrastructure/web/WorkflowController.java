package qc.levis.contrat.processus.infrastructure.web;

import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import qc.levis.contrat.processus.application.WorkflowService;
import qc.levis.contrat.processus.application.dto.WorkflowRequest;

/**
 * Concepteur de workflow d'un formulaire (écran 5). Gardé par le module « processus » et par rôle de
 * conception. La définition (étapes, conditions de routage par montant) est stockée en JSON.
 */
@RestController
@RequestMapping("/api/formulaires/{fid}/workflow")
@PreAuthorize("@modules.actif('processus') and hasAnyRole('ADMIN','REDACTEUR','SUPERADMIN')")
public class WorkflowController {

    private final WorkflowService service;

    public WorkflowController(WorkflowService service) {
        this.service = service;
    }

    @GetMapping
    public Map<String, String> lire(@PathVariable Long fid) {
        return Map.of("definitionJson", service.lire(fid));
    }

    @PutMapping
    public Map<String, String> enregistrer(@PathVariable Long fid, @RequestBody WorkflowRequest req) {
        return Map.of("definitionJson", service.enregistrer(fid, req.definitionJson()));
    }
}
