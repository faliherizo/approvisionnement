package qc.levis.contrat.clm.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.clm.application.port.AlertesExploitation;
import qc.levis.contrat.clm.application.port.PerimetreUtilisateur;
import qc.levis.contrat.clm.application.port.PortSalesforce;
import qc.levis.contrat.clm.application.port.SourceAleatoire;
import qc.levis.contrat.clm.application.repository.MessageSortantRepository;
import qc.levis.contrat.clm.domain.MessageSortant;
import qc.levis.contrat.clm.domain.Relances;

/** ST-14 : un retour vers Salesforce qui épuise ses reprises entre en file d'erreur et alerte l'exploitation, une seule fois. */
class SalesforceServiceAlerteTest {

    private static final Instant T0 = Instant.parse("2026-11-02T10:00:00Z");

    private final PortSalesforce port = mock(PortSalesforce.class);
    private final MessageSortantRepository sorties = mock(MessageSortantRepository.class);
    private final AlertesExploitation alertes = mock(AlertesExploitation.class);
    private final ContexteClm ctx = new ContexteClm(mock(PerimetreUtilisateur.class), new PorteeTenantMemoire(), new ObjectMapper(),
            Clock.fixed(T0, ZoneOffset.UTC));
    private final SalesforceService service = new SalesforceService(port, sorties, mock(SourceAleatoire.class),
            new ClmProprietes(null, null, null, null, null, null, null), ctx, alertes);

    private MessageSortant enAttente(int echecsPrecedents) {
        MessageSortant m = new MessageSortant(7L, 10L, "DEM-2026-0001", "LANCEE", "10:LANCEE:3", "corr-1", "{}", T0);
        for (int i = 0; i < echecsPrecedents; i++) {
            m.echouer("panne", T0);
        }
        when(sorties.findTop50ByStatutAndProchainEssaiLessThanEqualOrderByProchainEssaiAsc(eq(MessageSortant.Statut.EN_ATTENTE), any()))
                .thenReturn(List.of(m));
        doThrow(new PortSalesforce.Indisponible("Salesforce indisponible.")).when(port).publierStatut(any());
        return m;
    }

    @Test
    void la_derniere_reprise_en_echec_place_le_retour_en_file_d_erreur_et_alerte() {
        MessageSortant m = enAttente(Relances.REPRISES_MAX - 1);

        assertEquals(0, service.relayer());

        assertEquals(MessageSortant.Statut.ECHEC, m.getStatut());
        verify(alertes).signaler(AlertesExploitation.Code.SALESFORCE_FILE_ERREUR, 7L, "corrélation corr-1",
                "LANCEE · " + Relances.REPRISES_MAX + " tentatives");
    }

    @Test
    void un_echec_qui_laisse_une_reprise_planifiee_n_alerte_pas() {
        MessageSortant m = enAttente(0);

        service.relayer();

        assertEquals(MessageSortant.Statut.EN_ATTENTE, m.getStatut());
        verifyNoInteractions(alertes);
    }
}
