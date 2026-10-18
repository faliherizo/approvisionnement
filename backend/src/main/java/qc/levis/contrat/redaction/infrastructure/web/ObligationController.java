package qc.levis.contrat.redaction.infrastructure.web;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import qc.levis.contrat.redaction.application.ObligationService;
import qc.levis.contrat.redaction.application.dto.ObligationRequest;
import qc.levis.contrat.redaction.domain.Obligation;

/** Obligations & échéances contractuelles. */
@RestController
@RequestMapping("/api")
public class ObligationController {

    private final ObligationService service;

    public ObligationController(ObligationService service) {
        this.service = service;
    }

    private String utilisateur(Authentication auth) {
        return auth != null ? auth.getName() : "anonyme";
    }

    @GetMapping("/documents/{documentId}/obligations")
    public List<Obligation> lister(@PathVariable Long documentId) {
        return service.lister(documentId);
    }

    @PostMapping("/documents/{documentId}/obligations")
    @PreAuthorize("hasAnyRole('REDACTEUR','APPRO','ADMIN')")
    public Obligation ajouter(@PathVariable Long documentId,
                              @Valid @RequestBody ObligationRequest req,
                              Authentication auth) {
        return service.ajouter(documentId, req, utilisateur(auth));
    }

    /** Échéances à venir dans les {@code jours} prochains (défaut 30). */
    @GetMapping("/obligations/a-venir")
    public List<Obligation> aVenir(@RequestParam(name = "jours", defaultValue = "30") int jours) {
        return service.aVenir(jours);
    }

    @PostMapping("/obligations/{id}/completer")
    @PreAuthorize("hasAnyRole('REDACTEUR','APPRO','ADMIN')")
    public Obligation completer(@PathVariable Long id, Authentication auth) {
        return service.completer(id, utilisateur(auth));
    }
}
