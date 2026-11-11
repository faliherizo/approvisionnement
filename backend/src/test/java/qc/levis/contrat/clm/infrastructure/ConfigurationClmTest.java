package qc.levis.contrat.clm.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.ZoneOffset;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import qc.levis.contrat.clm.application.JournalClm;
import qc.levis.contrat.clm.application.RappelService;
import qc.levis.contrat.clm.application.RappelsExternesService;
import qc.levis.contrat.clm.application.SalesforceService;
import qc.levis.contrat.clm.application.port.AlertesExploitation;
import qc.levis.contrat.clm.application.port.PortSalesforce;
import qc.levis.contrat.clm.infrastructure.web.WebClm;

/** Câblage du module : planificateur (rappels puis relais Salesforce), horloge UTC, traduction des erreurs techniques. */
class ConfigurationClmTest {

    private final JournalClm journal = mock(JournalClm.class);
    private final WebClm.Erreurs erreurs = new WebClm.Erreurs(journal);

    @AfterEach
    void nettoyer() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void le_planificateur_envoie_les_rappels_avant_de_relayer_les_statuts_puis_les_rappels_externes() {
        RappelService rappels = mock(RappelService.class);
        SalesforceService salesforce = mock(SalesforceService.class);
        RappelsExternesService externes = mock(RappelsExternesService.class);

        new ClmConfiguration.Planificateur(rappels, salesforce, externes, mock(AlertesExploitation.class)).executer();

        InOrder ordre = inOrder(rappels, salesforce, externes);
        ordre.verify(rappels).executer();
        ordre.verify(salesforce).relayer();
        ordre.verify(externes).relayer();
    }

    @Test
    void une_tache_du_planificateur_en_echec_est_signalee_sans_bloquer_la_suivante() {
        RappelService rappels = mock(RappelService.class);
        SalesforceService salesforce = mock(SalesforceService.class);
        AlertesExploitation alertes = mock(AlertesExploitation.class);
        when(rappels.executer()).thenThrow(new IllegalStateException("base indisponible"));

        RappelsExternesService externes = mock(RappelsExternesService.class);
        new ClmConfiguration.Planificateur(rappels, salesforce, externes, alertes).executer();

        verify(salesforce).relayer();
        verify(externes).relayer();
        verify(alertes).signaler(AlertesExploitation.Code.PLANIFICATEUR_ECHEC, null, "rappels et échéances", "IllegalStateException");
    }

    @Test
    void l_horloge_porte_le_fuseau_configure_sans_changer_les_instants() {
        java.time.Clock horloge = new ClmConfiguration().horloge("America/Toronto");
        assertEquals(java.time.ZoneId.of("America/Toronto"), horloge.getZone());
        assertTrue(Math.abs(horloge.millis() - System.currentTimeMillis()) < 5_000, "les instants restent universels");
    }

    @Test
    void salesforce_indisponible_donne_503_et_conflit_de_version_409() {
        ResponseEntity<Map<String, String>> panne = erreurs.salesforce(new PortSalesforce.Indisponible("Salesforce indisponible."));
        ResponseEntity<Map<String, String>> conflit = erreurs.concurrence(new OptimisticLockingFailureException("version"));

        assertEquals(503, panne.getStatusCode().value());
        assertEquals("Salesforce indisponible.", panne.getBody().get("message"));
        assertEquals(409, conflit.getStatusCode().value());
        assertTrue(conflit.getBody().get("message").contains("rechargez"));
    }

    @Test
    void un_refus_du_cadriciel_est_traduit_et_journalise_meme_sans_identite() {
        ResponseEntity<Map<String, String>> r = erreurs.refus(new AccessDeniedException("Access Denied"),
                new MockHttpServletRequest("GET", "/api/clm/admin/parametres"));

        assertEquals(403, r.getStatusCode().value());
        assertEquals("Accès refusé.", r.getBody().get("message"));
        verify(journal).refus(null, "GET /api/clm/admin/parametres", "Access Denied");
        assertEquals("Accès refusé.", erreurs.refus(new AccessDeniedException(null), new MockHttpServletRequest("GET", "/api/clm"))
                .getBody().get("message"), "sans motif, le message reste compréhensible");
    }

    @Test
    void un_refus_metier_garde_son_motif_et_designe_l_acteur() {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("direction@x.ca", null));
        ResponseEntity<Map<String, String>> r = erreurs.refus(new AccessDeniedException("Cette tâche ne vous est pas assignée."),
                new MockHttpServletRequest("POST", "/api/clm/etapes/7/decider"));

        assertEquals("Cette tâche ne vous est pas assignée.", r.getBody().get("message"));
        verify(journal).refus("direction@x.ca", "POST /api/clm/etapes/7/decider", "Cette tâche ne vous est pas assignée.");
    }
}
