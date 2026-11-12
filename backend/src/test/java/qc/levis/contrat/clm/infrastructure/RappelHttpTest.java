package qc.levis.contrat.clm.infrastructure;

import static org.junit.jupiter.api.Assertions.*;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.clm.application.port.PortRappel;

/** Transport des rappels : en-têtes de signature et d'idempotence, refus hors 2xx, hôte injoignable. */
class RappelHttpTest {

    private HttpServer serveur;
    private final List<String> recus = new ArrayList<>();
    private int statut = 204;
    private final RappelHttp http = new RappelHttp();

    @BeforeEach
    void demarrer() throws IOException {
        serveur = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        serveur.createContext("/r", ex -> {
            recus.add(ex.getRequestHeaders().getFirst("X-CLM-Signature") + "|" + ex.getRequestHeaders().getFirst("Idempotency-Key") + "|"
                    + new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            ex.sendResponseHeaders(statut, -1);
            ex.close();
        });
        serveur.start();
    }

    @AfterEach
    void arreter() {
        serveur.stop(0);
    }

    private String url() {
        return "http://127.0.0.1:" + serveur.getAddress().getPort() + "/r";
    }

    @Test
    void un_rappel_accepte_porte_sa_signature_et_sa_cle() {
        http.envoyer(url(), "{\"a\":1}", "sha256=ab", "7:1:LANCEE:1");
        assertEquals(List.of("sha256=ab|7:1:LANCEE:1|{\"a\":1}"), recus);
    }

    @Test
    void une_reponse_hors_2xx_ou_un_hote_injoignable_est_un_refus() {
        statut = 500;
        assertTrue(assertThrows(PortRappel.Refuse.class, () -> http.envoyer(url(), "{}", "s", "k")).getMessage().contains("500"));
        statut = 302;
        assertThrows(PortRappel.Refuse.class, () -> http.envoyer(url(), "{}", "s", "k"), "une redirection n'est pas suivie");
        serveur.stop(0);
        assertThrows(PortRappel.Refuse.class, () -> http.envoyer(url(), "{}", "s", "k"));
    }

    @Test
    void une_interruption_est_rendue_a_l_appelant() {
        Thread.currentThread().interrupt();
        try {
            assertThrows(PortRappel.Refuse.class, () -> http.envoyer(url(), "{}", "s", "k"));
        } finally {
            Thread.interrupted();
        }
    }
}
