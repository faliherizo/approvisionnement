package qc.levis.contrat.clm.infrastructure;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.clm.application.ConnexionsSalesforce;
import qc.levis.contrat.clm.application.port.PortSalesforce;
import qc.levis.contrat.clm.domain.ConnexionSalesforce;

/** Adaptateur Salesforce REST contre un serveur HTTP local qui imite l'API (OAuth, sobjects, SOQL, PATCH). */
class SalesforceRestTest {

    private HttpServer serveur;
    private final List<String> appels = new CopyOnWriteArrayList<>();
    private volatile int statutPatch = 204;
    private volatile int statutJeton = 200;
    private volatile int statutLimites = 503;
    private final AtomicInteger jetons = new AtomicInteger();
    private String base;
    private SalesforceRest sf;

    @BeforeEach
    void demarrer() throws IOException {
        serveur = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        serveur.createContext("/token", ex -> {
            jetons.incrementAndGet();
            repondre(ex, statutJeton, "{\"access_token\":\"jeton-test\"}");
        });
        serveur.createContext("/services/data/v60.0/sobjects/FundingRequest/a01", ex -> {
            appels.add(ex.getRequestMethod() + " " + ex.getRequestHeaders().getFirst("Authorization") + " " + ex.getRequestHeaders().getFirst("X-Correlation-Id"));
            if ("PATCH".equals(ex.getRequestMethod())) {
                appels.add(new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
                repondre(ex, statutPatch, "");
            } else {
                repondre(ex, 200, "{\"attributes\":{\"type\":\"FundingRequest\"},\"Id\":\"a01\",\"Name\":\"DEM-1\",\"Montant\":100}");
            }
        });
        serveur.createContext("/services/data/v60.0/sobjects/FundingRequest/DEM-2", ex -> repondre(ex, 404, "[]"));
        serveur.createContext("/services/data/v60.0/sobjects/FundingRequest/INCONNU", ex -> repondre(ex, 404, "[]"));
        serveur.createContext("/services/data/v60.0/sobjects/FundingRequest/EXPIRE", ex -> repondre(ex, 401, "[]"));
        serveur.createContext("/services/data/v60.0/sobjects/FundingRequest/ILLISIBLE", ex -> repondre(ex, 200, "<html>maintenance</html>"));
        serveur.createContext("/services/data/v60.0/query", ex -> {
            String q = ex.getRequestURI().getQuery();
            appels.add(q);
            repondre(ex, 200, q.contains("DEM-2") ? "{\"records\":[{\"attributes\":{},\"Id\":\"a02\",\"Name\":\"DEM-2\"}]}" : "{\"records\":[]}");
        });
        serveur.createContext("/services/data/v60.0/limits", ex -> repondre(ex, statutLimites, "{}"));
        serveur.start();
        base = "http://127.0.0.1:" + serveur.getAddress().getPort();
        sf = client(base, base + "/token");
    }

    private static SalesforceRest client(String instance, String jeton) {
        return new SalesforceRest(() -> connexion(null, instance, jeton, "secret", ConnexionsSalesforce.CHAMPS_PAR_DEFAUT),
                new ObjectMapper(), Clock.systemUTC());
    }

    private static ConnexionsSalesforce.Connexion connexion(Long proprietaire, String instance, String jeton, String secret,
                                                           ConnexionSalesforce.ChampsRetour champs) {
        return new ConnexionsSalesforce.Connexion(proprietaire, ConnexionSalesforce.Mode.REST, instance, jeton, "id", secret,
                "FundingRequest", "v60.0", champs, 8);
    }

    @AfterEach
    void arreter() {
        serveur.stop(0);
    }

    private static void repondre(HttpExchange ex, int statut, String corps) throws IOException {
        byte[] b = corps.getBytes(StandardCharsets.UTF_8);
        ex.sendResponseHeaders(statut, b.length == 0 ? -1 : b.length);
        if (b.length > 0) {
            ex.getResponseBody().write(b);
        }
        ex.close();
    }

    @Test
    void lecture_par_id_puis_par_numero_soql() {
        PortSalesforce.Dossier d = sf.lireDossier("a01").orElseThrow();
        assertEquals("DEM-1", d.numero());
        assertFalse(d.champs().containsKey("attributes"));
        assertTrue(appels.get(0).contains("Bearer jeton-test"));
        assertEquals("a02", sf.lireDossier("DEM-2").orElseThrow().id());
        assertTrue(sf.lireDossier("INCONNU").isEmpty());
        assertTrue(appels.stream().anyMatch(a -> a.replace('+', ' ').contains("Name = 'DEM-2'")), appels.toString());
    }

    @Test
    void retour_de_statut_porte_la_correlation_et_signale_les_refus() {
        sf.publierStatut(new PortSalesforce.Statut("a01", "LANCEE", "corr-1", "k", Map.of("reference", "TR-1")));
        assertTrue(appels.stream().anyMatch(a -> a.startsWith("PATCH") && a.endsWith("corr-1")));
        assertTrue(appels.stream().anyMatch(a -> a.contains("\"CLM_Statut__c\":\"LANCEE\"") && a.contains("TR-1")));
        statutPatch = 400;
        assertThrows(PortSalesforce.Indisponible.class, () -> sf.publierStatut(new PortSalesforce.Statut("a01", "X", "c", "k", Map.of())));
        statutPatch = 500;
        assertThrows(PortSalesforce.Indisponible.class, () -> sf.publierStatut(new PortSalesforce.Statut("a01", "X", "c", "k", Map.of())));
    }

    @Test
    void test_de_connexion_et_authentification_refusee() {
        assertFalse(sf.tester().disponible());
        statutJeton = 401;
        assertThrows(PortSalesforce.Indisponible.class, () -> client(base, base + "/token").lireDossier("a01"));
        assertFalse(client("http://127.0.0.1:1", "http://127.0.0.1:1/token").tester().disponible());
    }

    @Test
    void connexion_etablie_quand_l_api_repond() {
        statutLimites = 200;
        PortSalesforce.EtatConnexion etat = sf.tester();
        assertTrue(etat.disponible());
        assertTrue(etat.detail().contains("v60.0"), etat.detail());
    }

    @Test
    void un_jeton_refuse_par_l_api_est_redemande_au_prochain_appel() {
        assertThrows(PortSalesforce.Indisponible.class, () -> sf.lireDossier("EXPIRE"));
        assertEquals(1, jetons.get());
        sf.lireDossier("a01");
        assertEquals(2, jetons.get(), "le 401 invalide le jeton : une nouvelle authentification précède l'appel suivant");
    }

    @Test
    void une_reponse_illisible_ou_une_instance_injoignable_sont_des_indisponibilites() {
        assertEquals("Réponse Salesforce illisible.",
                assertThrows(PortSalesforce.Indisponible.class, () -> sf.lireDossier("ILLISIBLE")).getMessage());
        SalesforceRest coupee = client("http://127.0.0.1:1", base + "/token");
        assertEquals("Salesforce injoignable.", assertThrows(PortSalesforce.Indisponible.class, () -> coupee.lireDossier("a01")).getMessage());
    }

    @Test
    void un_appel_interrompu_rend_l_interruption_a_l_appelant() {
        SalesforceRest neuf = client(base, base + "/token");
        Thread.currentThread().interrupt();
        PortSalesforce.Indisponible authentification = assertThrows(PortSalesforce.Indisponible.class, () -> neuf.lireDossier("a01"));
        assertTrue(Thread.interrupted(), "l'interruption est rétablie pour l'appelant");
        assertEquals("Authentification Salesforce interrompue.", authentification.getMessage());

        sf.lireDossier("a01");
        Thread.currentThread().interrupt();
        PortSalesforce.Indisponible appel = assertThrows(PortSalesforce.Indisponible.class, () -> sf.lireDossier("a01"));
        assertTrue(Thread.interrupted(), "l'interruption est rétablie pour l'appelant");
        assertEquals("Appel Salesforce interrompu.", appel.getMessage());
    }

    @Test
    void un_statut_non_serialisable_est_refuse_avant_tout_envoi() {
        assertThrows(IllegalStateException.class,
                () -> sf.publierStatut(new PortSalesforce.Statut("a01", "X", "c", "k", Map.of("reference", new Object()))));
        assertTrue(appels.isEmpty(), appels.toString());
        assertEquals(0, jetons.get(), "aucune authentification n'a été tentée");
    }

    @Test
    void le_retour_de_statut_alimente_les_champs_choisis_par_l_organisation() {
        ConnexionSalesforce.ChampsRetour champs = new ConnexionSalesforce.ChampsRetour("Etat_CLM__c", "No_Trousse__c", "Trace__c");
        SalesforceRest propre = new SalesforceRest(() -> connexion(42L, base, base + "/token", "secret", champs), new ObjectMapper(),
                Clock.systemUTC());
        propre.publierStatut(new PortSalesforce.Statut("a01", "SIGNEE", "corr-9", "k", Map.of("reference", "IQ-1")));
        assertTrue(appels.stream().anyMatch(a -> a.contains("\"Etat_CLM__c\":\"SIGNEE\"") && a.contains("\"No_Trousse__c\":\"IQ-1\"")
                && a.contains("\"Trace__c\":\"corr-9\"")), appels.toString());
        assertTrue(appels.stream().noneMatch(a -> a.contains("CLM_Statut__c")), "aucun champ de la plateforme n'est écrit");
    }

    @Test
    void chaque_connexion_obtient_son_jeton_et_un_secret_change_en_exige_un_nouveau() {
        AtomicReference<ConnexionsSalesforce.Connexion> courante = new AtomicReference<>(
                connexion(1L, base, base + "/token", "secret-1", ConnexionsSalesforce.CHAMPS_PAR_DEFAUT));
        SalesforceRest partage = new SalesforceRest(courante::get, new ObjectMapper(), Clock.systemUTC());
        partage.lireDossier("a01");
        partage.lireDossier("a01");
        assertEquals(1, jetons.get(), "le jeton de l'organisation 1 est réutilisé");
        courante.set(connexion(2L, base, base + "/token", "secret-2", ConnexionsSalesforce.CHAMPS_PAR_DEFAUT));
        partage.lireDossier("a01");
        assertEquals(2, jetons.get(), "l'organisation 2 ne réutilise jamais le jeton de l'organisation 1");
        courante.set(connexion(2L, base, base + "/token", "secret-2-rotation", ConnexionsSalesforce.CHAMPS_PAR_DEFAUT));
        partage.lireDossier("a01");
        assertEquals(3, jetons.get(), "un secret remplacé exige une nouvelle authentification");
    }
}
