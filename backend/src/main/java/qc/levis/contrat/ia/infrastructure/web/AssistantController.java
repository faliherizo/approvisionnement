package qc.levis.contrat.ia.infrastructure.web;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import qc.levis.contrat.ia.application.AssistantGuideService;
import qc.levis.contrat.ia.application.dto.AssistantContexteRequest;
import qc.levis.contrat.ia.application.dto.AssistantGenerationRequest;
import qc.levis.contrat.ia.application.dto.AssistantSuggestion;
import qc.levis.contrat.redaction.domain.Document;

/** Assistant guidé de génération de documents (art. 7.2, écart 5). */
@RestController
@RequestMapping("/api/assistant")
public class AssistantController {

    private final AssistantGuideService service;

    public AssistantController(AssistantGuideService service) {
        this.service = service;
    }

    /** Recommandations (mode, templates, clauses éligibles) pour le contexte saisi. */
    @PostMapping("/analyser")
    public AssistantSuggestion analyser(@RequestBody AssistantContexteRequest req) {
        return service.analyser(req);
    }

    /** Génère le document guidé (template ou sélection manuelle, rattachement optionnel). */
    @PostMapping("/generer")
    @PreAuthorize("hasAnyRole('REDACTEUR','APPRO','ADMIN')")
    public Document generer(@Valid @RequestBody AssistantGenerationRequest req, Authentication auth) {
        return service.generer(req, auth != null ? auth.getName() : "anonyme");
    }
}
