package qc.levis.contrat.redaction.infrastructure.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import qc.levis.contrat.redaction.application.ModeleDocumentService;
import qc.levis.contrat.redaction.application.dto.ModeleDocumentRequest;
import qc.levis.contrat.redaction.domain.ModeleDocument;

/**
 * Modèle de mise en forme des documents générés (gabarit propre à l'entreprise).
 * L'admin configure celui de son tenant ; le superadmin (contexte global) le défaut.
 */
@RestController
@RequestMapping("/api/modele-document")
public class ModeleDocumentController {

    private final ModeleDocumentService service;

    public ModeleDocumentController(ModeleDocumentService service) {
        this.service = service;
    }

    @GetMapping
    public ModeleDocument courant() {
        return service.courant();
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
    public ModeleDocument enregistrer(@RequestBody ModeleDocumentRequest req) {
        return service.enregistrer(req);
    }
}
