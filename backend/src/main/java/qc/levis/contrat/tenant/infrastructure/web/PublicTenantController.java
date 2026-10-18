package qc.levis.contrat.tenant.infrastructure.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import qc.levis.contrat.tenant.application.TenantAdminService;
import qc.levis.contrat.tenant.application.dto.TenantPublic;
import qc.levis.contrat.tenant.application.dto.TenantResume;

/** Information publique d'un tenant (titre de connexion), accessible sans session. */
@RestController
@RequestMapping("/api/tenants")
public class PublicTenantController {

    private final TenantAdminService service;

    public PublicTenantController(TenantAdminService service) {
        this.service = service;
    }

    @GetMapping("/{slug}/public")
    public TenantPublic parSlug(@PathVariable String slug) {
        return service.publicParSlug(slug);
    }

    /** Liste des tenants (résumé) pour le sélecteur de connexion (dev). */
    @GetMapping
    public java.util.List<TenantResume> lister() {
        return service.listerPublic();
    }
}
