package qc.levis.contrat.reference.infrastructure.web;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import qc.levis.contrat.reference.application.ReferenceService;
import qc.levis.contrat.reference.application.dto.CategorieVue;
import qc.levis.contrat.reference.application.dto.ReferenceRequest;
import qc.levis.contrat.reference.application.dto.ValeurReference;

/** Valeurs de la plateforme (superadmin) : les valeurs par défaut de chaque liste, pour toutes les organisations. */
@RestController
@RequestMapping("/api/superadmin/reference")
@PreAuthorize("hasRole('SUPERADMIN')")
public class SuperadminReferenceController {

    private final ReferenceService service;

    public SuperadminReferenceController(ReferenceService service) {
        this.service = service;
    }

    @GetMapping("/categories")
    public List<CategorieVue> categories() {
        return service.categories();
    }

    @GetMapping
    public List<ValeurReference> lister(@RequestParam String categorie) {
        return service.lister(categorie, true);
    }

    @PostMapping
    public ValeurReference creer(@Valid @RequestBody ReferenceRequest req) {
        return service.creer(req, true);
    }

    @PutMapping("/{id}")
    public ValeurReference modifier(@PathVariable Long id, @Valid @RequestBody ReferenceRequest req) {
        return service.modifier(id, req, true);
    }

    @DeleteMapping("/{id}")
    public void supprimer(@PathVariable Long id) {
        service.supprimer(id, true);
    }
}
