package qc.levis.contrat.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.integration.application.IntegrationSapService.ConfigSap;
import qc.levis.contrat.integration.application.IntegrationSapService;
import qc.levis.contrat.integration.application.port.PublicationCiblePort.ResultatPublication;
import qc.levis.contrat.integration.application.port.ReferenceFinancierePort.ReferenceFinanciere;
import qc.levis.contrat.integration.infrastructure.SapODataAdapter;
import qc.levis.contrat.integration.infrastructure.SimulationSapAdapter;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Validation de l'adaptateur SAP OData <b>par tenant</b> : dépôt réel contre le {@link FakeSapServer}
 * quand le tenant a une config active, repli sur la simulation sinon. Vérifie URL, authentification
 * (Basic/APIKey), corps envoyé, parsing de la réponse et chemins d'échec — sans SAP réel.
 */
class SapODataAdapterTest {

    private static final String SERVICE = "/ZREF_APPRO_SRV/References";

    private FakeSapServer serveur;
    private IntegrationSapService config;
    private SimulationSapAdapter simulation;
    private SapODataAdapter adaptateur;

    private final ReferenceFinanciere reference =
            new ReferenceFinanciere(42L, "7000017905", "Acquisition solution", 3, new BigDecimal("150000"));

    @BeforeEach
    void setUp() {
        serveur = new FakeSapServer();
        serveur.demarrer();
        config = mock(IntegrationSapService.class);
        simulation = mock(SimulationSapAdapter.class);
        adaptateur = new SapODataAdapter(new ObjectMapper(), config, simulation);
        TenantContext.set(1L);
    }

    @AfterEach
    void tearDown() {
        serveur.arreter();
        TenantContext.clear();
    }

    private void configReelle(String auth, String user, String pass, String apiKey, String baseUrl) {
        when(config.configClient(1L)).thenReturn(Optional.of(
                new ConfigSap(baseUrl, SERVICE, auth, user, pass, apiKey)));
    }

    @Test
    void deposer_basic_envoieLaReference_etParseLaReponse() {
        configReelle("basic", "SAP_USER", "SAP_PWD", "", serveur.baseUrl());
        ResultatPublication r = adaptateur.deposer(reference);

        assertTrue(r.succes());
        assertEquals("sap://SAP-REF-1", r.url());
        assertEquals("POST", serveur.derniereMethode);
        assertEquals(SERVICE, serveur.dernierChemin);
        assertTrue(serveur.dernierCorps.contains("7000017905"));
        assertTrue(serveur.dernierCorps.contains("150000"));
        String attendu = "Basic " + Base64.getEncoder()
                .encodeToString("SAP_USER:SAP_PWD".getBytes(StandardCharsets.UTF_8));
        assertEquals(attendu, serveur.derniereAutorisation);
        verify(simulation, never()).deposer(any());
    }

    @Test
    void deposer_apikey_envoieLEnTeteApiKey() {
        configReelle("apikey", "", "", "CLE-SANDBOX-123", serveur.baseUrl());
        adaptateur.deposer(reference);
        assertEquals("CLE-SANDBOX-123", serveur.dernierApiKey);
        assertNull(serveur.derniereAutorisation);
    }

    @Test
    void deposer_reponseNonJson_repliSurNumeroAO() {
        serveur.reponseNonJson = true;
        configReelle("basic", "u", "p", "", serveur.baseUrl());
        assertEquals("sap://7000017905", adaptateur.deposer(reference).url());
    }

    @Test
    void deposer_sansIdentifiants_aucunEnTeteAuth() {
        configReelle("basic", "", "", "", serveur.baseUrl());
        assertTrue(adaptateur.deposer(reference).succes());
        assertNull(serveur.derniereAutorisation);
        assertNull(serveur.dernierApiKey);
    }

    @Test
    void deposer_champsNuls_repliSurIdentifiantDossier() {
        serveur.reponseSansReference = true;
        configReelle("basic", "u", "p", "", serveur.baseUrl());
        ReferenceFinanciere refNulle = new ReferenceFinanciere(42L, null, null, 0, null);
        assertEquals("sap://dossier-42", adaptateur.deposer(refNulle).url());
    }

    @Test
    void deposer_echecServeur_renvoieEchec() {
        serveur.echecDepot = true;
        configReelle("basic", "u", "p", "", serveur.baseUrl());
        ResultatPublication r = adaptateur.deposer(reference);
        assertFalse(r.succes());
        assertEquals("POST", serveur.derniereMethode);
    }

    @Test
    void deposer_serveurInjoignable_echoueSansException() {
        configReelle("basic", "u", "p", "", "http://127.0.0.1:1");
        assertFalse(adaptateur.deposer(reference).succes());
    }

    @Test
    void tester_lectureReussie_confirmeLaConnexion() {
        ResultatPublication r = adaptateur.tester(
                new ConfigSap(serveur.baseUrl(), SERVICE, "basic", "u", "p", ""));
        assertTrue(r.succes());
        assertEquals("GET", serveur.derniereMethode);
        assertEquals(SERVICE, serveur.dernierChemin);       // le « ?$top=1 » n'est pas dans le path
    }

    @Test
    void tester_nonAutorise_echoue() {
        serveur.echecDepot = true;                          // GET → 401
        assertFalse(adaptateur.tester(
                new ConfigSap(serveur.baseUrl(), SERVICE, "apikey", "", "", "MAUVAISE-CLE")).succes());
    }

    @Test
    void tester_serveurInjoignable_echoue() {
        assertFalse(adaptateur.tester(
                new ConfigSap("http://127.0.0.1:1", SERVICE, "basic", "u", "p", "")).succes());
    }

    @Test
    void deposer_pasDeConfigActive_repliSurSimulation() {
        when(config.configClient(1L)).thenReturn(Optional.empty());
        when(simulation.deposer(reference))
                .thenReturn(new ResultatPublication(true, "sap://dossier/7000017905", "simulé"));

        ResultatPublication r = adaptateur.deposer(reference);
        assertTrue(r.succes());
        assertEquals("sap://dossier/7000017905", r.url());
        verify(simulation).deposer(reference);
        assertNull(serveur.derniereMethode); // aucune requête réseau
    }
}
