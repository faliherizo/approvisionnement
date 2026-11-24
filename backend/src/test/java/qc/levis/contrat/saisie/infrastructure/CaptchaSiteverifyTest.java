package qc.levis.contrat.saisie.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.saisie.application.port.CaptchaPublic;

/**
 * CHU 3.07 : l'adaptateur respecte le protocole siteverify de Turnstile et hCaptcha (formulaire secret/response,
 * réponse JSON success) et refuse le défi dès que le fournisseur ne le valide pas expressément.
 */
class CaptchaSiteverifyTest {

    private HttpServer fournisseur;
    private final List<String> recus = Collections.synchronizedList(new ArrayList<>());
    private volatile int statut = 200;
    private volatile String reponse = "{\"success\":true}";

    @BeforeEach
    void demarrer() throws Exception {
        fournisseur = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        fournisseur.createContext("/siteverify", ex -> {
            recus.add(ex.getRequestMethod() + " " + ex.getRequestHeaders().getFirst("Content-Type") + " "
                    + new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] corps = reponse.getBytes(StandardCharsets.UTF_8);
            ex.sendResponseHeaders(statut, corps.length);
            ex.getResponseBody().write(corps);
            ex.close();
        });
        fournisseur.start();
    }

    @AfterEach
    void arreter() {
        fournisseur.stop(0);
    }

    private CaptchaSiteverify adaptateur(String fournisseurNom) {
        return new CaptchaSiteverify(fournisseurNom, " cle-site ", "secret-essai",
                "http://127.0.0.1:" + fournisseur.getAddress().getPort() + "/siteverify");
    }

    @Test
    void jetonValide_selonLeProtocoleSiteverify() {
        assertThat(adaptateur("turnstile").verifier("jeton a&b")).isTrue();
        assertThat(recus.get(0)).isEqualTo("POST application/x-www-form-urlencoded secret=secret-essai&response=jeton+a%26b&sitekey=cle-site");
    }

    @Test
    void refusDuFournisseur_ouStatutInattendu_ouReponseIllisible_refusent() {
        reponse = "{\"success\":false,\"error-codes\":[\"invalid-input-response\"]}";
        assertThat(adaptateur("HCAPTCHA").verifier("jeton")).isFalse();
        reponse = "{\"success\":true}";
        statut = 500;
        assertThat(adaptateur("HCAPTCHA").verifier("jeton")).isFalse();
        statut = 200;
        reponse = "pas du json";
        assertThat(adaptateur("HCAPTCHA").verifier("jeton")).isFalse();
    }

    @Test
    void jetonAbsent_refuseSansAppeler() {
        assertThat(adaptateur("TURNSTILE").verifier(null)).isFalse();
        assertThat(adaptateur("TURNSTILE").verifier("  ")).isFalse();
        assertThat(recus).isEmpty();
    }

    @Test
    void fournisseurInjoignable_refuse() {
        var hs = adaptateur("TURNSTILE");
        fournisseur.stop(0);
        assertThat(hs.verifier("jeton")).isFalse();
    }

    @Test
    void interruption_refuse_etRetablitLeDrapeau() {
        Thread.currentThread().interrupt();
        try {
            assertThat(adaptateur("TURNSTILE").verifier("jeton")).isFalse();
            assertThat(Thread.currentThread().isInterrupted()).isTrue();
        } finally {
            Thread.interrupted();
        }
    }

    @Test
    void configurationPublique_neRevelePasLeSecret() {
        assertThat(adaptateur("hcaptcha").configurationPublique()).isEqualTo(new CaptchaPublic("HCAPTCHA", "cle-site"));
    }

    @Test
    void adresseParDefaut_celleDuFournisseur() {
        var t = new CaptchaSiteverify("TURNSTILE", "c", "s", " ");
        assertThat(t.configurationPublique().fournisseur()).isEqualTo("TURNSTILE");
        var h = new CaptchaSiteverify("HCAPTCHA", "c", "s", null);
        assertThat(h.configurationPublique().fournisseur()).isEqualTo("HCAPTCHA");
    }

    @Test
    void configurationIncomplete_echoueAuDemarrage() {
        assertThatThrownBy(() -> new CaptchaSiteverify("RECAPTCHA", "c", "s", "")).hasMessageContaining("inconnu");
        assertThatThrownBy(() -> new CaptchaSiteverify(null, "c", "s", "")).hasMessageContaining("inconnu");
        assertThatThrownBy(() -> new CaptchaSiteverify("TURNSTILE", "", "s", "")).hasMessageContaining("CAPTCHA_SECRET");
        assertThatThrownBy(() -> new CaptchaSiteverify("TURNSTILE", "c", null, "")).hasMessageContaining("CAPTCHA_SECRET");
    }

    @Test
    void simulation_accepteToutJetonNonVide() {
        var s = new CaptchaSimulation();
        assertThat(s.verifier("x")).isTrue();
        assertThat(s.verifier(" ")).isFalse();
        assertThat(s.verifier(null)).isFalse();
        assertThat(s.configurationPublique()).isEqualTo(CaptchaPublic.simulation());
    }
}
