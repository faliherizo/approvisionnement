package qc.levis.contrat.integration.infrastructure.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import qc.levis.contrat.integration.application.SapReferenceService;
import qc.levis.contrat.integration.application.port.PublicationCiblePort.ResultatPublication;

/** Transmission de la référence financière d'un dossier vers SAP (art. 9). */
@RestController
@RequestMapping("/api/dossiers/{id}/reference-sap")
public class SapReferenceController {

    private final SapReferenceService service;

    public SapReferenceController(SapReferenceService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('APPRO','ADMIN')")
    public ResultatPublication transmettre(@PathVariable Long id, Authentication auth) {
        return service.transmettre(id, auth != null ? auth.getName() : "anonyme");
    }
}
