package qc.levis.contrat.redaction.infrastructure.web;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import qc.levis.contrat.commun.module.infrastructure.web.ModuleRequis;
import qc.levis.contrat.redaction.application.GabaritService;
import qc.levis.contrat.redaction.application.dto.GabaritRequest;
import qc.levis.contrat.redaction.domain.Gabarit;

/**
 * Gestion dynamique des gabarits de documents normalisés. Tout utilisateur authentifié
 * consulte le catalogue effectif (globaux + surcharges de son tenant) ; la création,
 * modification, clonage et suppression sont réservés aux admins / superadmin.
 */
@RestController
@ModuleRequis("redaction")
@RequestMapping("/api/gabarits")
public class GabaritController {

    private final GabaritService service;

    public GabaritController(GabaritService service) {
        this.service = service;
    }

    @GetMapping
    public List<Gabarit> lister() {
        return service.lister();
    }

    @GetMapping("/{id}")
    public Gabarit obtenir(@PathVariable Long id) {
        return service.obtenir(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
    public Gabarit creer(@RequestBody GabaritRequest req) {
        return service.creer(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
    public Gabarit modifier(@PathVariable Long id, @RequestBody GabaritRequest req) {
        return service.modifier(id, req);
    }

    @PostMapping("/{id}/cloner")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
    public Gabarit cloner(@PathVariable Long id) {
        return service.cloner(id);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        service.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
