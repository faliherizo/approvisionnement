package qc.levis.contrat.commun.module.application;

import org.springframework.stereotype.Component;
import qc.levis.contrat.commun.infrastructure.TenantContext;

/**
 * Garde d'accès aux modules, exposé pour l'annotation de sécurité :
 * {@code @PreAuthorize("@modules.actif('formulaire')")}. Refus par défaut : si le module n'est pas
 * actif pour le tenant courant, l'accès est refusé (403). Le superadmin (tenant nul) a tout accès.
 */
@Component("modules")
public class ModuleAccess {

    private final ModuleService service;

    public ModuleAccess(ModuleService service) {
        this.service = service;
    }

    public boolean actif(String code) {
        return service.estActif(TenantContext.get(), code);
    }
}
