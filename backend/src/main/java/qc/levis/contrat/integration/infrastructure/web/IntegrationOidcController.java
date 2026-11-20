package qc.levis.contrat.integration.infrastructure.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import qc.levis.contrat.auth.infrastructure.OidcClient;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.integration.application.IntegrationOidcService;
import qc.levis.contrat.integration.application.dto.IntegrationOidcRequest;
import qc.levis.contrat.integration.application.dto.IntegrationOidcVue;
import qc.levis.contrat.integration.application.port.PublicationCiblePort.ResultatPublication;

/**
 * Configuration OpenID Connect du tenant courant (espace admin → Paramètres). L'administrateur du
 * tenant saisit et active sa propre connexion gouvernementale (Microsoft Entra ID ou tout IdP OIDC).
 * Le secret client n'est jamais renvoyé : seule sa présence est indiquée.
 */
@RestController
@RequestMapping("/api/parametres/integration-oidc")
@PreAuthorize("hasRole('ADMIN_TECHNIQUE')")
public class IntegrationOidcController {

    private final IntegrationOidcService service;
    private final OidcClient client;

    public IntegrationOidcController(IntegrationOidcService service, OidcClient client) {
        this.service = service;
        this.client = client;
    }

    @GetMapping
    public IntegrationOidcVue configuration() {
        return service.vue(TenantContext.get());
    }

    @PutMapping
    public IntegrationOidcVue enregistrer(@RequestBody IntegrationOidcRequest req) {
        return service.enregistrer(TenantContext.get(), req);
    }

    /**
     * Test de configuration : interroge la découverte OIDC ({@code .well-known/openid-configuration})
     * de l'émetteur du formulaire (ou celui déjà enregistré). Un 2xx confirme l'émetteur.
     */
    @PostMapping("/test")
    public ResultatPublication tester(@RequestBody IntegrationOidcRequest req) {
        return client.testerDecouverte(service.issuerPourTest(TenantContext.get(), req));
    }
}
