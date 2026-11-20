package qc.levis.contrat.reference.infrastructure.web;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import qc.levis.contrat.reference.application.ReferenceService;
import qc.levis.contrat.reference.application.dto.CategorieVue;
import qc.levis.contrat.reference.application.dto.ReferenceRequest;
import qc.levis.contrat.reference.application.dto.ValeurReference;

/**
 * Listes de référence de l'organisation (admin) : ajout, personnalisation ou retrait des valeurs de la plateforme.
 * Chaque liste relève du module qui la déclare : le service refuse celle d'un module non souscrit.
 */
@RestController
@RequestMapping("/api/admin/reference")
@PreAuthorize("hasRole('ADMIN_AFFAIRES')")
public class AdminReferenceController {

    private final ReferenceService service;

    public AdminReferenceController(ReferenceService service) {
        this.service = service;
    }

    @GetMapping("/categories")
    public List<CategorieVue> categories() {
        return service.categories();
    }

    @GetMapping
    public List<ValeurReference> lister(@RequestParam String categorie) {
        return service.lister(categorie, false);
    }

    @PostMapping
    public ValeurReference creer(@Valid @RequestBody ReferenceRequest req) {
        return service.creer(req, false);
    }

    @PutMapping("/{id}")
    public ValeurReference modifier(@PathVariable Long id, @Valid @RequestBody ReferenceRequest req) {
        return service.modifier(id, req, false);
    }

    @DeleteMapping("/{id}")
    public void supprimer(@PathVariable Long id) {
        service.supprimer(id, false);
    }
}
