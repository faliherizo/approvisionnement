package qc.levis.contrat.clm.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

import java.time.ZoneOffset;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.ResponseEntity;
import qc.levis.contrat.clm.application.RappelService;
import qc.levis.contrat.clm.application.SalesforceService;
import qc.levis.contrat.clm.application.port.PortSalesforce;
import qc.levis.contrat.clm.infrastructure.web.WebClm;

/** Câblage du module : planificateur (rappels puis relais Salesforce), horloge UTC, traduction des erreurs techniques. */
class ConfigurationClmTest {

    @Test
    void le_planificateur_envoie_les_rappels_avant_de_relayer_les_statuts_salesforce() {
        RappelService rappels = mock(RappelService.class);
        SalesforceService salesforce = mock(SalesforceService.class);

        new ClmConfiguration.Planificateur(rappels, salesforce).executer();

        InOrder ordre = inOrder(rappels, salesforce);
        ordre.verify(rappels).executer();
        ordre.verify(salesforce).relayer();
    }

    @Test
    void l_horloge_par_defaut_est_en_utc() {
        assertEquals(ZoneOffset.UTC, new ClmConfiguration().horloge().getZone());
    }

    @Test
    void salesforce_indisponible_donne_503_et_conflit_de_version_409() {
        WebClm.Erreurs erreurs = new WebClm.Erreurs();

        ResponseEntity<Map<String, String>> panne = erreurs.salesforce(new PortSalesforce.Indisponible("Salesforce indisponible."));
        ResponseEntity<Map<String, String>> conflit = erreurs.concurrence(new OptimisticLockingFailureException("version"));

        assertEquals(503, panne.getStatusCode().value());
        assertEquals("Salesforce indisponible.", panne.getBody().get("message"));
        assertEquals(409, conflit.getStatusCode().value());
        assertTrue(conflit.getBody().get("message").contains("rechargez"));
    }
}
