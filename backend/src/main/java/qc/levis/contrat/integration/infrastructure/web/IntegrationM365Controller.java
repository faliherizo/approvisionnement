package qc.levis.contrat.integration.infrastructure.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.commun.module.infrastructure.web.ModuleRequis;
import qc.levis.contrat.integration.application.IntegrationM365Service;
import qc.levis.contrat.integration.application.dto.IntegrationM365Request;
import qc.levis.contrat.integration.application.dto.IntegrationM365Vue;
import qc.levis.contrat.integration.application.port.Microsoft365Client;
import qc.levis.contrat.integration.application.port.PublicationCiblePort;

/**
 * Configuration Microsoft 365 du tenant courant (espace admin → Paramètres). L'administrateur du
 * tenant saisit et active sa propre intégration (SharePoint/OneDrive/Teams/Outlook). Le secret n'est
 * jamais renvoyé : seule sa présence est indiquée. La même connexion sert la publication du module de rédaction et la
 * diffusion des trousses terminées du module Contrats et signature (Annexe 10 d'IQ, TI-01 ; ADR-029).
 */
@RestController
@ModuleRequis({"redaction", "clm"})
@RequestMapping("/api/parametres/integration-m365")
@PreAuthorize("hasRole('ADMIN_TECHNIQUE')")
public class IntegrationM365Controller {

    private final IntegrationM365Service service;
    private final Microsoft365Client client;

    public IntegrationM365Controller(IntegrationM365Service service, Microsoft365Client client) {
        this.service = service;
        this.client = client;
    }

    @GetMapping
    public IntegrationM365Vue configuration() {
        return service.vue(TenantContext.get());
    }

    @PutMapping
    public IntegrationM365Vue enregistrer(@RequestBody IntegrationM365Request req) {
        return service.enregistrer(TenantContext.get(), req);
    }

    /** Test de connexion réel : obtient un jeton avec les identifiants enregistrés. */
    @PostMapping("/test")
    public PublicationCiblePort.ResultatPublication tester() {
        return service.configClient(TenantContext.get())
                .map(client::tester)
                .orElse(new PublicationCiblePort.ResultatPublication(false, null,
                        "Configuration Microsoft 365 absente, incomplète ou inactive."));
    }
}
