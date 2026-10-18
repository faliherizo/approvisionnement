package qc.levis.contrat.redaction.infrastructure.web;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import qc.levis.contrat.redaction.application.TemplateService;
import qc.levis.contrat.redaction.application.dto.InstancierTemplateRequest;
import qc.levis.contrat.redaction.application.dto.TemplateRequest;
import qc.levis.contrat.redaction.domain.Document;
import qc.levis.contrat.redaction.domain.DocumentTemplate;

/** Templates de documents nommés. */
@RestController
@RequestMapping("/api/templates")
public class TemplateController {

    private final TemplateService service;

    public TemplateController(TemplateService service) {
        this.service = service;
    }

    @GetMapping
    public List<DocumentTemplate> lister() {
        return service.lister();
    }

    @GetMapping("/{id}")
    public DocumentTemplate obtenir(@PathVariable Long id) {
        return service.obtenir(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','REDACTEUR')")
    public DocumentTemplate creer(@Valid @RequestBody TemplateRequest req) {
        return service.creer(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','REDACTEUR')")
    public DocumentTemplate modifier(@PathVariable Long id, @Valid @RequestBody TemplateRequest req) {
        return service.modifier(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','REDACTEUR')")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        service.supprimer(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/instancier")
    @PreAuthorize("hasAnyRole('REDACTEUR','ADMIN')")
    public Document instancier(@PathVariable Long id,
                               @Valid @RequestBody InstancierTemplateRequest req,
                               Authentication auth) {
        return service.instancier(id, req, auth != null ? auth.getName() : "anonyme");
    }
}
