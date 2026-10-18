package qc.levis.contrat.integration.infrastructure.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.integration.application.IntegrationSapService;
import qc.levis.contrat.integration.application.dto.IntegrationSapRequest;
import qc.levis.contrat.integration.application.dto.IntegrationSapVue;
import qc.levis.contrat.integration.application.port.PublicationCiblePort.ResultatPublication;
import qc.levis.contrat.integration.infrastructure.SapODataAdapter;

/**
 * Configuration SAP du tenant courant (espace admin → Paramètres). Chaque organisation branche son
 * propre SAP (service OData + authentification). Les secrets ne sont jamais renvoyés : seule leur
 * présence est indiquée.
 */
@RestController
@RequestMapping("/api/parametres/integration-sap")
@PreAuthorize("hasRole('ADMIN')")
public class IntegrationSapController {

    private final IntegrationSapService service;
    private final SapODataAdapter adapter;

    public IntegrationSapController(IntegrationSapService service, SapODataAdapter adapter) {
        this.service = service;
        this.adapter = adapter;
    }

    @GetMapping
    public IntegrationSapVue configuration() {
        return service.vue(TenantContext.get());
    }

    @PutMapping
    public IntegrationSapVue enregistrer(@RequestBody IntegrationSapRequest req) {
        return service.enregistrer(TenantContext.get(), req);
    }

    /**
     * Test de connexion : lecture du service OData avec la configuration <b>du formulaire</b> (sans
     * exiger l'enregistrement ni l'activation). Les secrets laissés vides reprennent ceux déjà stockés.
     */
    @PostMapping("/test")
    public ResultatPublication tester(@RequestBody IntegrationSapRequest req) {
        return service.configPourTest(TenantContext.get(), req)
                .map(adapter::tester)
                .orElse(new ResultatPublication(false, null,
                        "Renseignez l'URL de base et le chemin du service avant de tester."));
    }
}
