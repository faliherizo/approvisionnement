package qc.levis.contrat.clm.infrastructure.web;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import qc.levis.contrat.clm.application.ClientsIntegrationService;
import qc.levis.contrat.clm.application.mcp.ServeurMcp;
import qc.levis.contrat.clm.domain.ClientIntegration;
import qc.levis.contrat.commun.module.application.ModuleService;

/** ADR-023 : décisions du point d'accès MCP hors pile HTTP (en-tête mal formé, module non souscrit, version admise). */
class McpControllerTest {

    private final ServeurMcp serveur = mock(ServeurMcp.class);
    private final ClientsIntegrationService clients = mock(ClientsIntegrationService.class);
    private final ModuleService modules = mock(ModuleService.class);
    private final McpController controleur = new McpController(serveur, clients, modules, new ObjectMapper());
    private final ClientIntegration client = mock(ClientIntegration.class);

    @Test
    void un_en_tete_sans_bearer_n_est_pas_lu_comme_un_jeton() {
        when(clients.authentifier(null)).thenThrow(new ClientsIntegrationService.AccesRefuse());
        assertThrows(ClientsIntegrationService.AccesRefuse.class, () -> controleur.recevoir("Basic abc", null, "{}"));
        verify(clients).authentifier(isNull());
    }

    @Test
    void un_client_dont_l_organisation_n_a_pas_le_module_est_refuse() {
        when(client.getTenantId()).thenReturn(9L);
        when(clients.authentifier("jeton")).thenReturn(client);
        when(modules.estActif(9L, "clm")).thenReturn(false);
        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> controleur.recevoir("Bearer jeton", null, "{}"));
        assertEquals(403, e.getStatusCode().value());
    }

    @Test
    void une_version_admise_et_un_corps_absent_suivent_le_protocole() {
        when(client.getTenantId()).thenReturn(9L);
        when(clients.authentifier("jeton")).thenReturn(client);
        when(modules.estActif(9L, "clm")).thenReturn(true);
        when(serveur.traiter(same(client), any())).thenReturn(Optional.of(Map.of("jsonrpc", "2.0")));
        ResponseEntity<Object> r = controleur.recevoir("Bearer jeton", ServeurMcp.VERSIONS.get(0), "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"ping\"}");
        assertEquals(200, r.getStatusCode().value());
        when(serveur.traiter(same(client), any())).thenReturn(Optional.empty());
        assertEquals(202, controleur.recevoir("Bearer jeton", null, null).getStatusCode().value(), "corps absent : traité comme vide");
    }
}
