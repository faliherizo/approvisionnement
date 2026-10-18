package qc.levis.contrat.integration;

import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.commun.domain.Enums;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.integration.application.IntegrationM365Service;
import qc.levis.contrat.integration.application.port.Microsoft365Client;
import qc.levis.contrat.integration.application.port.PublicationCiblePort;
import qc.levis.contrat.integration.infrastructure.GraphPublicationAdapter;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Tests de la décision par tenant (mock vs réel) et de la délégation au client Graph. */
class GraphPublicationAdapterTest {

    private final IntegrationM365Service config = mock(IntegrationM365Service.class);
    private final Microsoft365Client client = mock(Microsoft365Client.class);
    private final GraphPublicationAdapter adaptateur = new GraphPublicationAdapter(config, client);

    private final Microsoft365Client.ConfigM365 cfg = new Microsoft365Client.ConfigM365(
            "t", "c", "s", "site", "drive", "team", "chan", "exp@ville.ca");
    private PublicationCiblePort.ContenuPublie contenu() {
        return new PublicationCiblePort.ContenuPublie(1L, "Contrat", "contrat-1.pdf",
                "application/pdf", new byte[]{1}, "Contrats");
    }

    @AfterEach
    void tearDown() { TenantContext.clear(); }

    @Test
    void supporte_cibleM365_tenantActifConfigure_vrai() {
        TenantContext.set(1L);
        when(config.actifEtConfigure(1L)).thenReturn(true);
        assertTrue(adaptateur.supporte(Enums.CiblePublication.SHAREPOINT));
        assertTrue(adaptateur.supporte(Enums.CiblePublication.TEAMS));
    }

    @Test
    void supporte_tenantInactif_faux_donneLaMainAlaSimulation() {
        TenantContext.set(1L);
        when(config.actifEtConfigure(1L)).thenReturn(false);
        assertFalse(adaptateur.supporte(Enums.CiblePublication.SHAREPOINT));
    }

    @Test
    void supporte_sap_faux_memeSiActif() {
        TenantContext.set(1L);
        when(config.actifEtConfigure(1L)).thenReturn(true);
        assertFalse(adaptateur.supporte(Enums.CiblePublication.SAP));
    }

    @Test
    void deposer_delegueAuClient_etRenvoieSonResultat() {
        TenantContext.set(1L);
        when(config.configClient(1L)).thenReturn(Optional.of(cfg));
        when(client.deposer(eq(cfg), eq(Enums.CiblePublication.SHAREPOINT), any()))
                .thenReturn(new PublicationCiblePort.ResultatPublication(true, "https://sp/contrat", "ok"));

        PublicationCiblePort.ResultatPublication r =
                adaptateur.deposer(Enums.CiblePublication.SHAREPOINT, contenu());
        assertTrue(r.succes());
        assertEquals("https://sp/contrat", r.url());
    }

    @Test
    void deposer_configAbsente_echecPropre() {
        TenantContext.set(1L);
        when(config.configClient(1L)).thenReturn(Optional.empty());
        PublicationCiblePort.ResultatPublication r =
                adaptateur.deposer(Enums.CiblePublication.SHAREPOINT, contenu());
        assertFalse(r.succes());
        assertNull(r.url());
    }

    @Test
    void deposer_exceptionDuClient_estCapturee() {
        TenantContext.set(1L);
        when(config.configClient(1L)).thenReturn(Optional.of(cfg));
        when(client.deposer(any(), any(), any())).thenThrow(new IllegalStateException("boom"));
        PublicationCiblePort.ResultatPublication r =
                adaptateur.deposer(Enums.CiblePublication.ONEDRIVE, contenu());
        assertFalse(r.succes());
        assertTrue(r.message().contains("boom"));
    }
}
