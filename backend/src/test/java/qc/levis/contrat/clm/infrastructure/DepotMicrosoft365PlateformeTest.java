package qc.levis.contrat.clm.infrastructure;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import qc.levis.contrat.clm.application.port.DepotMicrosoft365;
import qc.levis.contrat.clm.domain.DiffusionM365;
import qc.levis.contrat.commun.domain.Enums;
import qc.levis.contrat.integration.application.port.PublicationCiblePort;

/** Le dépôt du module passe par les adaptateurs de publication de la plateforme, sans réécrire de protocole (ADR-029). */
class DepotMicrosoft365PlateformeTest {

    private static final DepotMicrosoft365.Fichier FICHIER = new DepotMicrosoft365.Fichier(9L, "Entente", "e.pdf", "application/pdf",
            new byte[]{1}, "Contrats/TR-1");

    @Test
    void le_premier_adaptateur_qui_sait_deposer_l_emporte_et_recoit_le_fichier_tel_quel() {
        PublicationCiblePort graph = mock(PublicationCiblePort.class);
        PublicationCiblePort simulation = mock(PublicationCiblePort.class);
        when(graph.supporte(any())).thenReturn(false);
        when(simulation.supporte(Enums.CiblePublication.ONEDRIVE)).thenReturn(true);
        when(simulation.deposer(eq(Enums.CiblePublication.ONEDRIVE), any()))
                .thenReturn(new PublicationCiblePort.ResultatPublication(true, "sim://onedrive/x", "ok"));

        DepotMicrosoft365.Resultat r = new DepotMicrosoft365Plateforme(List.of(graph, simulation))
                .deposer(DepotMicrosoft365.Destination.de(DiffusionM365.Bibliotheque.ONEDRIVE), FICHIER);

        assertEquals(new DepotMicrosoft365.Resultat(true, "sim://onedrive/x", "ok"), r);
        ArgumentCaptor<PublicationCiblePort.ContenuPublie> c = ArgumentCaptor.forClass(PublicationCiblePort.ContenuPublie.class);
        verify(simulation).deposer(eq(Enums.CiblePublication.ONEDRIVE), c.capture());
        assertEquals("Contrats/TR-1", c.getValue().destination());
        assertEquals("e.pdf", c.getValue().nomFichier());
        assertEquals(9L, c.getValue().documentId());
        verify(graph, never()).deposer(any(), any());
        assertEquals(DepotMicrosoft365.Destination.SHAREPOINT, DepotMicrosoft365.Destination.de(DiffusionM365.Bibliotheque.SHAREPOINT));
    }

    @Test
    void sans_adaptateur_ou_sur_panne_le_resultat_le_dit_sans_exception() {
        assertFalse(new DepotMicrosoft365Plateforme(List.of()).deposer(DepotMicrosoft365.Destination.TEAMS, FICHIER).succes());
        PublicationCiblePort casse = mock(PublicationCiblePort.class);
        when(casse.supporte(any())).thenReturn(true);
        when(casse.deposer(any(), any())).thenThrow(new IllegalStateException("Graph 503"));
        DepotMicrosoft365.Resultat r = new DepotMicrosoft365Plateforme(List.of(casse)).deposer(DepotMicrosoft365.Destination.TEAMS, FICHIER);
        assertFalse(r.succes());
        assertEquals("Échec Microsoft 365 : Graph 503", r.message());
    }
}
