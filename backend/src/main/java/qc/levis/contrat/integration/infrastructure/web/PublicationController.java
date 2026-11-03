package qc.levis.contrat.integration.infrastructure.web;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import qc.levis.contrat.commun.module.infrastructure.web.ModuleRequis;
import qc.levis.contrat.integration.application.PublicationService;
import qc.levis.contrat.integration.application.dto.PublicationRequest;
import qc.levis.contrat.integration.domain.PublicationLog;

/** Publication d'un document vers les cibles externes M365/SAP (art. 9, ADR-002). */
@RestController
@ModuleRequis("redaction")
@RequestMapping("/api/documents/{id}/publications")
public class PublicationController {

    private final PublicationService service;

    public PublicationController(PublicationService service) {
        this.service = service;
    }

    @GetMapping
    public List<PublicationLog> historique(@PathVariable Long id) {
        return service.historique(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('REDACTEUR','APPRO','ADMIN')")
    public PublicationLog publier(@PathVariable Long id,
                                  @Valid @RequestBody PublicationRequest req,
                                  Authentication auth) {
        return service.publier(id, req, auth != null ? auth.getName() : "anonyme");
    }
}
