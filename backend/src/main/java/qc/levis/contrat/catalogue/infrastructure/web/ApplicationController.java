package qc.levis.contrat.catalogue.infrastructure.web;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import qc.levis.contrat.catalogue.application.ApplicationService;
import qc.levis.contrat.catalogue.application.dto.ApplicationRequest;
import qc.levis.contrat.catalogue.application.dto.ApplicationVue;

/**
 * Catalogue des applications / espaces fonctionnels (écran 2). Gardé par le module « application » et par
 * rôle d'administration.
 */
@RestController("catalogueApplicationController")
@RequestMapping("/api/applications")
@PreAuthorize("@modules.actif('application') and hasAnyRole('ADMIN','SUPERADMIN')")
public class ApplicationController {

    private final ApplicationService service;

    public ApplicationController(ApplicationService service) {
        this.service = service;
    }

    @GetMapping
    public List<ApplicationVue> lister(@RequestParam(required = false) String domaine) {
        return service.lister(domaine);
    }

    @PostMapping
    public ApplicationVue creer(@Valid @RequestBody ApplicationRequest req) {
        return service.creer(req);
    }

    @PutMapping("/{id}")
    public ApplicationVue modifier(@PathVariable Long id, @Valid @RequestBody ApplicationRequest req) {
        return service.modifier(id, req);
    }

    @PutMapping("/{id}/archiver")
    public ApplicationVue archiver(@PathVariable Long id, @RequestParam boolean actif) {
        return service.archiver(id, actif);
    }

    @PostMapping("/{id}/dupliquer")
    public ApplicationVue dupliquer(@PathVariable Long id, @RequestParam String code, @RequestParam String nom) {
        return service.dupliquer(id, code, nom);
    }
}
