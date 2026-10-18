package qc.levis.contrat.integration.infrastructure.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import qc.levis.contrat.auth.infrastructure.SamlClient;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.integration.application.IntegrationSamlService;
import qc.levis.contrat.integration.application.dto.IntegrationSamlRequest;
import qc.levis.contrat.integration.application.dto.IntegrationSamlVue;
import qc.levis.contrat.integration.application.port.PublicationCiblePort.ResultatPublication;

/**
 * Configuration SAML 2.0 (Service Provider) du tenant courant (espace admin → Paramètres).
 * L'administrateur du tenant saisit et active sa propre fédération SAML (ADFS, Azure AD, Shibboleth,
 * tout IdP SAML). Le certificat de signature de l'IdP est public : il est renvoyé tel quel.
 */
@RestController
@RequestMapping("/api/parametres/integration-saml")
@PreAuthorize("hasRole('ADMIN')")
public class IntegrationSamlController {

    private final IntegrationSamlService service;
    private final SamlClient client;

    public IntegrationSamlController(IntegrationSamlService service, SamlClient client) {
        this.service = service;
        this.client = client;
    }

    @GetMapping
    public IntegrationSamlVue configuration() {
        return service.vue(TenantContext.get());
    }

    @PutMapping
    public IntegrationSamlVue enregistrer(@RequestBody IntegrationSamlRequest req) {
        return service.enregistrer(TenantContext.get(), req);
    }

    /**
     * Test de configuration : vérifie que la configuration est complète (EntityID, URL SSO,
     * certificat) et que le certificat X.509 de l'IdP est bien parsable. Aucun appel réseau.
     */
    @PostMapping("/test")
    public ResultatPublication tester(@RequestBody IntegrationSamlRequest req) {
        return client.testerConfiguration(service.pourTest(TenantContext.get(), req));
    }
}
