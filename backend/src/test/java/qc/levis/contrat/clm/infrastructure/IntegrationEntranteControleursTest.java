package qc.levis.contrat.clm.infrastructure;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import qc.levis.contrat.clm.application.ClientsIntegrationService;
import qc.levis.contrat.clm.application.DemandesSignatureService;
import qc.levis.contrat.clm.application.DocumentClmService;
import qc.levis.contrat.clm.application.PreuveService;
import qc.levis.contrat.clm.application.RappelsExternesService;
import qc.levis.contrat.clm.application.dto.ClmDto;
import qc.levis.contrat.clm.domain.ClientIntegration;
import qc.levis.contrat.clm.infrastructure.web.IntegrationEntranteController;
import qc.levis.contrat.clm.infrastructure.web.IntegrationsEntrantesAdminController;
import qc.levis.contrat.commun.module.application.ModuleService;

/** Décisions des contrôleurs d'intégration : en-tête absent, module non souscrit, délégation aux cas d'utilisation. */
class IntegrationEntranteControleursTest {

    private final ClientsIntegrationService clients = mock(ClientsIntegrationService.class);
    private final DemandesSignatureService demandes = mock(DemandesSignatureService.class);
    private final ModuleService modules = mock(ModuleService.class);
    private final IntegrationEntranteController api = new IntegrationEntranteController(clients, demandes, mock(DocumentClmService.class),
            mock(PreuveService.class), modules);
    private final ClientIntegration client = ClientIntegration.creer(7L, "Workday", ClientIntegration.Systeme.WORKDAY, null, "clm-1", "e", "k",
            "admin", Instant.EPOCH);

    @Test
    void sans_en_tete_bearer_le_jeton_est_absent_et_refuse() {
        when(clients.authentifier(null)).thenThrow(new ClientsIntegrationService.AccesRefuse());
        assertThrows(ClientsIntegrationService.AccesRefuse.class, () -> api.trousse(null, 1L));
        assertThrows(ClientsIntegrationService.AccesRefuse.class, () -> api.trousse("Basic abc", 1L));
        assertEquals(HttpStatus.UNAUTHORIZED, api.refuse(new ClientsIntegrationService.AccesRefuse()).getStatusCode());
    }

    @Test
    void un_client_dont_l_organisation_n_a_pas_le_module_est_refuse() {
        when(clients.authentifier("j")).thenReturn(client);
        when(modules.estActif(7L, "clm")).thenReturn(false);
        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> api.trousse("Bearer j", 1L));
        assertEquals(HttpStatus.FORBIDDEN, e.getStatusCode());
        verifyNoInteractions(demandes);
    }

    @Test
    void un_client_autorise_lit_sa_trousse() {
        when(clients.authentifier("j")).thenReturn(client);
        when(modules.estActif(7L, "clm")).thenReturn(true);
        ClmDto.TrousseExterneVue vue = new ClmDto.TrousseExterneVue(1L, "TR-1", "BROUILLON", "WORKDAY", "WD-1", null, null, List.of());
        when(demandes.trousseDuClient(client, 1L)).thenReturn(vue);
        assertSame(vue, api.trousse("Bearer j", 1L));
    }

    @Test
    void l_administration_relance_un_rappel() {
        RappelsExternesService rappels = mock(RappelsExternesService.class);
        ClmDto.RappelVue vue = new ClmDto.RappelVue(5L, "Workday", 1L, "LANCEE", "EN_ATTENTE", 0, null, Instant.EPOCH, null);
        when(rappels.relancer(5L)).thenReturn(vue);
        assertSame(vue, new IntegrationsEntrantesAdminController(clients, rappels).relancer(5L));
    }
}
