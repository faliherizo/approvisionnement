package qc.levis.contrat.clm.infrastructure;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Appels Entra ID des adaptateurs Microsoft (ADR-036, ADR-037) : jeton en cache, réponses refusées, pannes de transport. */
class ClientEntraTest {

    private HttpServer serveur;
    private String base;
    private final AtomicInteger jetons = new AtomicInteger();
    private Instant maintenant = Instant.parse("2026-11-26T12:00:00Z");
    private ClientEntra client;
    private ClientEntra.Identite qui;

    @BeforeEach
    void demarrer() throws Exception {
        serveur = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        base = "http://127.0.0.1:" + serveur.getAddress().getPort();
        serveur.createContext("/t/oauth2/v2.0/token", x -> repondre(x, 200, "{\"access_token\":\"j" + jetons.incrementAndGet() + "\",\"expires_in\":120}"));
        serveur.createContext("/vide", x -> repondre(x, 204, ""));
        serveur.createContext("/refus", x -> repondre(x, 403, "{\"secret\":\"jamais relu\"}"));
        serveur.createContext("/illisible", x -> repondre(x, 200, "{pas du json"));
        serveur.createContext("/echo", x -> repondre(x, 200, "{\"methode\":\"" + x.getRequestMethod() + "\",\"auth\":\""
                + x.getRequestHeaders().getFirst("Authorization") + "\"}"));
        serveur.start();
        Clock horloge = new Clock() {
            @Override public ZoneOffset getZone() { return ZoneOffset.UTC; }
            @Override public Clock withZone(java.time.ZoneId z) { return this; }
            @Override public Instant instant() { return maintenant; }
        };
        client = new ClientEntra(new ObjectMapper(), horloge);
        qui = new ClientEntra.Identite(base, "t", "c", "s", "scope");
    }

    @AfterEach
    void arreter() {
        serveur.stop(0);
    }

    private static void repondre(com.sun.net.httpserver.HttpExchange x, int statut, String corps) throws java.io.IOException {
        byte[] o = corps.getBytes(StandardCharsets.UTF_8);
        x.sendResponseHeaders(statut, o.length == 0 ? -1 : o.length);
        if (o.length > 0) {
            x.getResponseBody().write(o);
        }
        x.close();
    }

    @Test
    void le_jeton_sert_jusqu_a_une_minute_de_son_expiration() {
        assertEquals("GET", client.get(qui, base + "/echo").get("methode").asText());
        assertEquals("POST", client.post(qui, base + "/echo", Map.of("a", 1)).get("methode").asText());
        assertEquals("Bearer j1", client.patch(qui, base + "/echo", Map.of()).get("auth").asText());
        assertEquals(1, jetons.get());
        maintenant = maintenant.plusSeconds(61);
        client.get(qui, base + "/echo");
        assertEquals(2, jetons.get(), "jeton renouvelé passé expires_in - 60 s");
    }

    @Test
    void une_reponse_vide_est_un_objet_vide_et_un_refus_porte_le_statut_sans_le_corps() {
        assertTrue(client.post(qui, base + "/vide", Map.of()).isEmpty());
        ClientEntra.Refus r = assertThrows(ClientEntra.Refus.class, () -> client.get(qui, base + "/refus"));
        assertEquals(403, r.statut());
        assertFalse(r.getMessage().contains("jamais relu"));
        assertThrows(IllegalStateException.class, () -> client.get(qui, base + "/illisible"));
        assertThrows(ClientEntra.Refus.class, () -> client.get(new ClientEntra.Identite(base, "autre", "c", "s", "x"), base + "/echo"));
    }

    @Test
    void un_service_injoignable_ou_un_appel_interrompu_leve_une_erreur_d_etat() {
        assertThrows(IllegalStateException.class, () -> client.get(new ClientEntra.Identite("http://127.0.0.1:9", "t", "c", "s", "x"), base));
        Thread.currentThread().interrupt();
        try {
            assertThrows(IllegalStateException.class, () -> client.get(qui, base + "/echo"));
            assertTrue(Thread.interrupted(), "l'interruption est rétablie");
        } finally {
            Thread.interrupted();
        }
    }

    @Test
    void une_requete_impossible_a_serialiser_est_refusee() {
        Object circulaire = new Object() {
            @SuppressWarnings("unused")
            public Object getSoi() {
                return this;
            }
        };
        assertThrows(IllegalStateException.class, () -> client.post(qui, base + "/echo", circulaire));
    }
}
