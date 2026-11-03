package qc.levis.contrat.reference.infrastructure.web;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import qc.levis.contrat.commun.module.infrastructure.web.ModuleRequis;
import qc.levis.contrat.reference.application.ReferenceService;
import qc.levis.contrat.reference.application.dto.ReferenceRequest;
import qc.levis.contrat.reference.domain.ReferenceValue;

/** Gestion des valeurs de référence SPÉCIFIQUES au tenant (admin). */
@RestController
@ModuleRequis({"redaction", "formulaire"})
@RequestMapping("/api/admin/reference")
@PreAuthorize("hasRole('ADMIN')")
public class AdminReferenceController {

    private final ReferenceService service;

    public AdminReferenceController(ReferenceService service) {
        this.service = service;
    }

    @GetMapping
    public List<ReferenceValue> lister(@RequestParam String categorie) {
        return service.lister(categorie, false);
    }

    @PostMapping
    public ReferenceValue creer(@Valid @RequestBody ReferenceRequest req) {
        return service.creer(req, false);
    }

    @PutMapping("/{id}")
    public ReferenceValue modifier(@PathVariable Long id, @Valid @RequestBody ReferenceRequest req) {
        return service.modifier(id, req, false);
    }

    @DeleteMapping("/{id}")
    public void supprimer(@PathVariable Long id) {
        service.supprimer(id, false);
    }
}
