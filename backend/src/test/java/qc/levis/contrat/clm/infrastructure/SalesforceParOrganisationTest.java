package qc.levis.contrat.clm.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.clm.application.ConnexionsSalesforce;
import qc.levis.contrat.clm.application.port.PortSalesforce;
import qc.levis.contrat.clm.domain.ConnexionSalesforce;

/** Chaque appel suit le mode de la connexion de l'organisation en cours : API réelle ou simulation. */
class SalesforceParOrganisationTest {

    private final ConnexionsSalesforce connexions = mock(ConnexionsSalesforce.class);
    private final SalesforceRest rest = mock(SalesforceRest.class);
    private final SalesforceSimulation simulation = mock(SalesforceSimulation.class);
    private final SalesforceParOrganisation port = new SalesforceParOrganisation(connexions, rest, simulation);

    private void mode(ConnexionSalesforce.Mode mode) {
        when(connexions.courante()).thenReturn(new ConnexionsSalesforce.Connexion(7L, mode, null, null, null, null, "FundingRequest",
                "v60.0", ConnexionsSalesforce.CHAMPS_PAR_DEFAUT, 8));
    }

    @Test
    void une_organisation_en_rest_joint_l_api_reelle() {
        mode(ConnexionSalesforce.Mode.REST);
        PortSalesforce.EtatConnexion etat = new PortSalesforce.EtatConnexion(true, "rest", "ok");
        when(rest.tester()).thenReturn(etat);
        when(rest.lireDossier("a01", Set.of())).thenReturn(Optional.empty());
        PortSalesforce.Statut statut = new PortSalesforce.Statut("a01", "LANCEE", "c", "k", java.util.Map.of());

        assertEquals(etat, port.tester());
        assertEquals(Optional.empty(), port.lireDossier("a01", Set.of()));
        port.publierStatut(statut);

        verify(rest).publierStatut(statut);
        verifyNoInteractions(simulation);
    }

    @Test
    void une_organisation_en_simulation_reste_dans_la_simulation() {
        mode(ConnexionSalesforce.Mode.SIMULATION);
        PortSalesforce.Statut statut = new PortSalesforce.Statut("a01", "LANCEE", "c", "k", java.util.Map.of());

        port.tester();
        port.lireDossier("DEM-1", Set.of());
        port.publierStatut(statut);

        verify(simulation).tester();
        verify(simulation).lireDossier("DEM-1", Set.of());
        verify(simulation).publierStatut(statut);
        verifyNoInteractions(rest);
    }
}
