package qc.levis.contrat.soumission.infrastructure.web;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import qc.levis.contrat.commun.module.infrastructure.web.ModuleRequis;
import qc.levis.contrat.soumission.application.BordereauService;
import qc.levis.contrat.soumission.application.dto.BordereauRequest;
import qc.levis.contrat.soumission.domain.Bordereau;

/** Bordereau de prix d'un document contractuel. */
@RestController
@ModuleRequis("redaction")
@RequestMapping("/api/documents/{documentId}/bordereau")
public class BordereauController {

    private final BordereauService service;

    public BordereauController(BordereauService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<Bordereau> get(@PathVariable Long documentId) {
        Bordereau b = service.get(documentId);
        return b == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(b);
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('REDACTEUR','ADMIN')")
    public Bordereau enregistrer(@PathVariable Long documentId,
                                 @Valid @RequestBody BordereauRequest req, Authentication auth) {
        return service.enregistrer(documentId, req, auth != null ? auth.getName() : "anonyme");
    }
}
