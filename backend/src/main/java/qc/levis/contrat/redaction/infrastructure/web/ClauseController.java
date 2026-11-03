package qc.levis.contrat.redaction.infrastructure.web;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.commun.module.infrastructure.web.ModuleRequis;
import qc.levis.contrat.redaction.application.ClauseService;
import qc.levis.contrat.redaction.application.dto.ClauseRequest;
import qc.levis.contrat.redaction.application.repository.ClauseRepository;
import qc.levis.contrat.redaction.domain.Clause;
import qc.levis.contrat.tenant.application.dto.PageResponse;

@RestController
@ModuleRequis("redaction")
@RequestMapping("/api/clauses")
public class ClauseController {

    private final ClauseService service;
    private final ClauseRepository clauses;

    public ClauseController(ClauseService service, ClauseRepository clauses) {
        this.service = service;
        this.clauses = clauses;
    }

    /** Liste complète (non paginée) — conservée pour l'assemblage et les sélecteurs. */
    @GetMapping
    public List<Clause> lister() {
        return clauses.findByActifTrueAndTenantId(TenantContext.get());
    }

    /** Recherche paginée pour la gestion de la bibliothèque. */
    @GetMapping("/recherche")
    public PageResponse<Clause> rechercher(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int taille,
            @RequestParam(defaultValue = "") String q) {
        return service.lister(page, taille, q);
    }

    @GetMapping("/{id}")
    public Clause get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','JURIDIQUE','REDACTEUR')")
    public Clause creer(@Valid @RequestBody ClauseRequest req) {
        return service.creer(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','JURIDIQUE','REDACTEUR')")
    public Clause modifier(@PathVariable Long id, @Valid @RequestBody ClauseRequest req) {
        return service.modifier(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','JURIDIQUE')")
    public void supprimer(@PathVariable Long id) {
        service.supprimer(id);
    }
}
