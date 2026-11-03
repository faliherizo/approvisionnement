package qc.levis.contrat.temps.infrastructure.web;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import qc.levis.contrat.temps.application.FeuilleTempsService;
import qc.levis.contrat.temps.application.dto.FeuilleTempsRequest;
import qc.levis.contrat.temps.application.dto.FeuilleTempsVue;

/**
 * Feuilles de temps (Espace RH). Gardé par le module « rh ». Chaque utilisateur gère ses feuilles ;
 * l'approbation (approuver/refuser) est réservée aux rôles gestionnaires.
 */
@RestController
@RequestMapping("/api/rh/feuilles-temps")
@PreAuthorize("@modules.actif('rh')")
public class FeuilleTempsController {

    private final FeuilleTempsService service;

    public FeuilleTempsController(FeuilleTempsService service) {
        this.service = service;
    }

    @GetMapping("/mine")
    public List<FeuilleTempsVue> mesFeuilles(Authentication auth) {
        return service.mesFeuilles(auth != null ? auth.getName() : null);
    }

    @GetMapping("/a-approuver")
    @PreAuthorize("@modules.actif('rh') and hasAnyRole('ADMIN','DIRECTION','APPRO','SUPERADMIN')")
    public List<FeuilleTempsVue> aApprouver() {
        return service.aApprouver();
    }

    @GetMapping("/{id}")
    public FeuilleTempsVue obtenir(@PathVariable Long id) {
        return service.obtenir(id);
    }

    @PostMapping
    public FeuilleTempsVue creer(@RequestBody FeuilleTempsRequest req, Authentication auth) {
        return service.creer(req, auth != null ? auth.getName() : null);
    }

    @PutMapping("/{id}")
    public FeuilleTempsVue majLignes(@PathVariable Long id, @RequestBody FeuilleTempsRequest req) {
        return service.majLignes(id, req);
    }

    @PostMapping("/{id}/soumettre")
    public FeuilleTempsVue soumettre(@PathVariable Long id) {
        return service.soumettre(id);
    }

    @PostMapping("/{id}/approuver")
    @PreAuthorize("@modules.actif('rh') and hasAnyRole('ADMIN','DIRECTION','APPRO','SUPERADMIN')")
    public FeuilleTempsVue approuver(@PathVariable Long id, Authentication auth) {
        return service.approuver(id, auth != null ? auth.getName() : null);
    }

    @PostMapping("/{id}/refuser")
    @PreAuthorize("@modules.actif('rh') and hasAnyRole('ADMIN','DIRECTION','APPRO','SUPERADMIN')")
    public FeuilleTempsVue refuser(@PathVariable Long id, @RequestParam(required = false) String motif, Authentication auth) {
        return service.refuser(id, auth != null ? auth.getName() : null, motif);
    }
}
