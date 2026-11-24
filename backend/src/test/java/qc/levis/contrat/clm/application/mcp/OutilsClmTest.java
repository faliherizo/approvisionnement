package qc.levis.contrat.clm.application.mcp;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.clm.application.DemandesSignatureService;
import qc.levis.contrat.clm.application.dto.ClmDto;
import qc.levis.contrat.clm.domain.ClientIntegration;

/** ADR-023 : un outil MCP lit par le cas d'utilisation de l'API d'intégration et rend un objet JSON. */
class OutilsClmTest {

    @Test
    void lire_lot_rend_l_avancement_du_lot_du_client() {
        DemandesSignatureService demandes = mock(DemandesSignatureService.class);
        ClientIntegration client = mock(ClientIntegration.class);
        when(demandes.lotDuClient(client, 5L)).thenReturn(new ClmDto.LotVue(5L, "Lot RH", 3L, "API", "TERMINE", 4, 3, 1, true,
                "integration:essai", Instant.parse("2026-11-22T10:00:00Z"), List.of()));
        OutilMcp outil = new OutilsClm().lireLot(demandes);
        Map<String, Object> r = outil.executer(client, Map.of("lotId", 5));
        assertEquals(4, r.get("total"));
        assertEquals(1, r.get("echecs"));
        assertEquals("lire_lot", outil.nom());
        verify(demandes).lotDuClient(same(client), eq(5L));
    }
}
