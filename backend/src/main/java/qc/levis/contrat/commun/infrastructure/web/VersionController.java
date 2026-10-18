package qc.levis.contrat.commun.infrastructure.web;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import qc.levis.contrat.redaction.application.DocumentVersionService.DiffClause;
import qc.levis.contrat.redaction.application.DocumentVersionService;
import qc.levis.contrat.redaction.application.dto.VersionRequest;
import qc.levis.contrat.redaction.domain.DocumentVersion;

/** Versionnage et comparaison (redlining) des documents. */
@RestController
@RequestMapping("/api")
public class VersionController {

    private final DocumentVersionService service;

    public VersionController(DocumentVersionService service) {
        this.service = service;
    }

    @GetMapping("/documents/{documentId}/versions")
    public List<DocumentVersion> lister(@PathVariable Long documentId) {
        return service.lister(documentId);
    }

    @PostMapping("/documents/{documentId}/versions")
    @PreAuthorize("hasAnyRole('REDACTEUR','APPRO','ADMIN')")
    public DocumentVersion capturer(@PathVariable Long documentId,
                                    @RequestBody(required = false) VersionRequest req,
                                    Authentication auth) {
        String note = req != null ? req.note() : null;
        return service.capturer(documentId, note, auth != null ? auth.getName() : "anonyme");
    }

    @GetMapping("/versions/comparer")
    public List<DiffClause> comparer(@RequestParam("a") Long versionA, @RequestParam("b") Long versionB) {
        return service.comparer(versionA, versionB);
    }
}
