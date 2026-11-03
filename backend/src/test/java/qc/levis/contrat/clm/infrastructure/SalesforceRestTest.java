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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.clm.application.ClmProprietes;
import qc.levis.contrat.clm.application.port.PortSalesforce;

/** Adaptateur Salesforce REST contre un serveur HTTP local qui imite l'API (OAuth, sobjects, SOQL, PATCH). */
class SalesforceRestTest {

    private HttpServer serveur;
    private final List<String> appels = new CopyOnWriteArrayList<>();
    private volatile int statutPatch = 204;
    private volatile int statutJeton = 200;
    private SalesforceRest sf;

    @BeforeEach
    void demarrer() throws IOException {
        serveur = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        serveur.createContext("/token", ex -> repondre(ex, statutJeton, "{\"access_token\":\"jeton-test\"}"));
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
        serveur.createContext("/services/data/v60.0/query", ex -> {
            String q = ex.getRequestURI().getQuery();
            appels.add(q);
            repondre(ex, 200, q.contains("DEM-2") ? "{\"records\":[{\"attributes\":{},\"Id\":\"a02\",\"Name\":\"DEM-2\"}]}" : "{\"records\":[]}");
        });
        serveur.createContext("/services/data/v60.0/limits", ex -> repondre(ex, 503, "{}"));
        serveur.start();
        String base = "http://127.0.0.1:" + serveur.getAddress().getPort();
        ClmProprietes p = new ClmProprietes(null, null, null, null, null, null,
                new ClmProprietes.Salesforce("rest", base, base + "/token", "id", "secret", "FundingRequest", "v60.0"));
        sf = new SalesforceRest(p, new ObjectMapper(), Clock.systemUTC());
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
        SalesforceRest neuf = new SalesforceRest(new ClmProprietes(null, null, null, null, null, null,
                new ClmProprietes.Salesforce("rest", "http://127.0.0.1:" + serveur.getAddress().getPort(),
                        "http://127.0.0.1:" + serveur.getAddress().getPort() + "/token", "id", "s", "FundingRequest", "v60.0")),
                new ObjectMapper(), Clock.systemUTC());
        assertThrows(PortSalesforce.Indisponible.class, () -> neuf.lireDossier("a01"));
        SalesforceRest injoignable = new SalesforceRest(new ClmProprietes(null, null, null, null, null, null,
                new ClmProprietes.Salesforce("rest", "http://127.0.0.1:1", "http://127.0.0.1:1/token", "id", "s", "FundingRequest", "v60.0")),
                new ObjectMapper(), Clock.systemUTC());
        assertFalse(injoignable.tester().disponible());
    }
}
