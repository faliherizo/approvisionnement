package qc.levis.contrat.commun.module.infrastructure.web;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.commun.module.application.ModuleService;
import qc.levis.contrat.commun.module.application.dto.ModuleVue;

/** Modules actifs pour le tenant courant (consommé par le frontend pour afficher/masquer les vues). */
@RestController
@RequestMapping("/api/modules")
public class ModuleController {

    private final ModuleService service;

    public ModuleController(ModuleService service) {
        this.service = service;
    }

    @GetMapping
    public List<ModuleVue> mesModules() {
        return service.etatPourTenant(TenantContext.get());
    }
}
