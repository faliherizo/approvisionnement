package qc.levis.contrat.rh.infrastructure.web;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import qc.levis.contrat.rh.application.RhService;
import qc.levis.contrat.rh.application.dto.EmployeRequest;
import qc.levis.contrat.rh.application.dto.EmployeVue;

/** Dossiers employés (RH, écran Personnel / Dossier). Gardé par le module « rh » et par rôle RH. */
@RestController
@RequestMapping("/api/rh/employes")
@PreAuthorize("@modules.actif('rh') and hasAnyRole('ADMIN','DIRECTION','SUPERADMIN')")
public class RhController {

    private final RhService service;

    public RhController(RhService service) {
        this.service = service;
    }

    @GetMapping
    public List<EmployeVue> lister() {
        return service.lister();
    }

    @GetMapping("/{id}")
    public EmployeVue obtenir(@PathVariable Long id) {
        return service.obtenir(id);
    }

    @PostMapping
    public EmployeVue creer(@Valid @RequestBody EmployeRequest req) {
        return service.creer(req);
    }

    @PutMapping("/{id}")
    public EmployeVue modifier(@PathVariable Long id, @Valid @RequestBody EmployeRequest req) {
        return service.modifier(id, req);
    }

    @PutMapping("/{id}/archiver")
    public EmployeVue archiver(@PathVariable Long id, @RequestParam boolean actif) {
        return service.archiver(id, actif);
    }
}
