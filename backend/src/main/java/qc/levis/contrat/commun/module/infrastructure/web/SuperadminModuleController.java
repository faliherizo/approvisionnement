package qc.levis.contrat.commun.module.infrastructure.web;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import qc.levis.contrat.commun.module.application.ModuleService;
import qc.levis.contrat.commun.module.application.dto.ModuleToggleResult;
import qc.levis.contrat.commun.module.application.dto.ModuleVue;
import qc.levis.contrat.commun.module.domain.ModuleCatalogue;

/** Gestion des modules activables/désactivables par tenant (superadmin). */
@RestController
@RequestMapping("/api/superadmin")
@PreAuthorize("hasRole('SUPERADMIN')")
public class SuperadminModuleController {

    private final ModuleService service;

    public SuperadminModuleController(ModuleService service) {
        this.service = service;
    }

    /** Catalogue complet des modules. */
    @GetMapping("/modules")
    public List<ModuleCatalogue> catalogue() {
        return service.catalogue();
    }

    /** État des modules pour un tenant donné. */
    @GetMapping("/tenants/{tenantId}/modules")
    public List<ModuleVue> etat(@PathVariable Long tenantId) {
        return service.etatPourTenant(tenantId);
    }

    @PutMapping("/tenants/{tenantId}/modules/{code}/activer")
    public ModuleToggleResult activer(@PathVariable Long tenantId, @PathVariable String code, Authentication auth) {
        return service.activer(tenantId, code, auth != null ? auth.getName() : "superadmin");
    }

    @PutMapping("/tenants/{tenantId}/modules/{code}/desactiver")
    public ModuleToggleResult desactiver(@PathVariable Long tenantId, @PathVariable String code, Authentication auth) {
        return service.desactiver(tenantId, code, auth != null ? auth.getName() : "superadmin");
    }
}
