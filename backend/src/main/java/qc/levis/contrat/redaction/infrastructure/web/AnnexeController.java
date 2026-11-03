package qc.levis.contrat.redaction.infrastructure.web;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import qc.levis.contrat.commun.module.infrastructure.web.ModuleRequis;
import qc.levis.contrat.redaction.application.AnnexeService;
import qc.levis.contrat.redaction.application.dto.AnnexeRequest;
import qc.levis.contrat.redaction.domain.DocumentAnnexe;

/** Annexes d'un document contractuel. */
@RestController
@ModuleRequis("redaction")
@RequestMapping("/api/documents/{documentId}/annexes")
public class AnnexeController {

    private final AnnexeService service;

    public AnnexeController(AnnexeService service) {
        this.service = service;
    }

    private String utilisateur(Authentication auth) {
        return auth != null ? auth.getName() : "anonyme";
    }

    @GetMapping
    public List<DocumentAnnexe> lister(@PathVariable Long documentId) {
        return service.lister(documentId);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('REDACTEUR','ADMIN')")
    public DocumentAnnexe ajouter(@PathVariable Long documentId,
                                  @Valid @RequestBody AnnexeRequest req, Authentication auth) {
        return service.ajouter(documentId, req, utilisateur(auth));
    }

    @DeleteMapping("/{annexeId}")
    @PreAuthorize("hasAnyRole('REDACTEUR','ADMIN')")
    public void supprimer(@PathVariable Long documentId, @PathVariable Long annexeId) {
        service.supprimer(documentId, annexeId);
    }
}
