package qc.levis.contrat.notification.infrastructure;

import static org.junit.jupiter.api.Assertions.*;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import qc.levis.contrat.commun.infrastructure.reseau.GardeSortante;
import qc.levis.contrat.notification.application.port.CanalNotification;

/**
 * RST-17 : l'adaptateur Twilio respecte le contrat de l'API publiée (chemin, authentification Basic, formulaire) et
 * transforme un refus de Twilio en « ECHEC » relançable. Le faux Twilio répond comme le vrai (201 + sid, 400 + code).
 */
class SmsTwilioTest {

    private HttpServer twilio;
    private final List<String[]> recus = Collections.synchronizedList(new ArrayList<>());
    private volatile int statut = 201;
    private volatile String reponse = "{\"sid\":\"SM123\",\"status\":\"queued\"}";

    @BeforeEach
    void demarrer() throws Exception {
        twilio = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        twilio.createContext("/", ex -> {
            recus.add(new String[]{ex.getRequestMethod(), ex.getRequestURI().getPath(), ex.getRequestHeaders().getFirst("Authorization"),
                    ex.getRequestHeaders().getFirst("Content-Type"), new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8)});
            byte[] corps = reponse.getBytes(StandardCharsets.UTF_8);
            ex.sendResponseHeaders(statut, corps.length);
            ex.getResponseBody().write(corps);
            ex.close();
        });
        twilio.start();
    }

    @AfterEach
    void arreter() {
        twilio.stop(0);
    }

    private SmsTwilio adaptateur() {
        return new SmsTwilio("http://127.0.0.1:" + twilio.getAddress().getPort() + "/", "AC00000000000000000000000000000000",
                "jeton-essai", "+15145550000", new GardeSortante(Set.of("*"), h -> new java.net.InetAddress[0]));
    }

    @Test
    void un_sms_est_envoye_selon_le_contrat_de_l_api_twilio() {
        assertEquals("SMS", adaptateur().canal());
        assertEquals("ENVOYE", adaptateur().envoyer("+15145550101", "ignoré", "Votre code : 123456"));
        String[] r = recus.get(0);
        assertEquals("POST", r[0]);
        assertEquals("/2010-04-01/Accounts/AC00000000000000000000000000000000/Messages.json", r[1]);
        assertEquals("Basic " + Base64.getEncoder().encodeToString("AC00000000000000000000000000000000:jeton-essai".getBytes(StandardCharsets.UTF_8)), r[2]);
        assertEquals("application/x-www-form-urlencoded", r[3]);
        String formulaire = URLDecoder.decode(r[4], StandardCharsets.UTF_8);
        assertTrue(formulaire.contains("To=+15145550101"), formulaire);
        assertTrue(formulaire.contains("From=+15145550000"));
        assertTrue(formulaire.contains("Body=Votre code : 123456"));
    }

    @Test
    void un_refus_de_twilio_rend_un_echec_avec_son_code() {
        statut = 400;
        reponse = "{\"code\":21211,\"message\":\"Invalid 'To' Phone Number\"}";
        assertEquals("ECHEC", adaptateur().envoyer("+1999", "s", "c"));
        reponse = "pas du json";
        assertEquals("ECHEC", adaptateur().envoyer("+1999", "s", "c"));
    }

    @Test
    void un_fournisseur_injoignable_ou_une_destination_refusee_rend_un_echec() {
        twilio.stop(0);
        assertEquals("ECHEC", adaptateur().envoyer("+15145550101", "s", "c"));
        SmsTwilio garde = new SmsTwilio("http://169.254.169.254", "AC1", "t", "+1", new GardeSortante(Set.of(), java.net.InetAddress::getAllByName));
        assertEquals("ECHEC", garde.envoyer("+15145550101", "s", "c"));
    }

    @Test
    void sans_compte_jeton_ou_numero_expediteur_l_adaptateur_refuse_de_demarrer() {
        GardeSortante g = GardeSortante.aucune();
        assertThrows(IllegalStateException.class, () -> new SmsTwilio("https://api.twilio.com", "", "t", "+1", g));
        assertThrows(IllegalStateException.class, () -> new SmsTwilio("https://api.twilio.com", "AC1", " ", "+1", g));
        assertThrows(IllegalStateException.class, () -> new SmsTwilio("https://api.twilio.com", "AC1", "t", null, g));
    }

    @Test
    void le_numero_n_est_jamais_journalise_en_entier() {
        assertEquals("…01", SmsTwilio.masquer("+15145550101"));
        assertEquals("?", SmsTwilio.masquer(null));
        assertEquals("?", SmsTwilio.masquer("1"));
    }

    @Test
    void le_canal_sms_se_choisit_par_configuration_simulation_par_defaut() {
        ApplicationContextRunner contexte = new ApplicationContextRunner()
                .withBean(GardeSortante.class, GardeSortante::aucune)
                .withUserConfiguration(SmsSimulation.class, SmsTwilio.class)
                .withPropertyValues("app.notification.sms.twilio.url=https://api.twilio.com", "app.notification.sms.twilio.account-sid=AC1",
                        "app.notification.sms.twilio.auth-token=t", "app.notification.sms.twilio.from=+1");
        contexte.run(c -> assertInstanceOf(SmsSimulation.class, c.getBean(CanalNotification.class)));
        contexte.withPropertyValues("app.notification.sms.mode=twilio")
                .run(c -> assertInstanceOf(SmsTwilio.class, c.getBean(CanalNotification.class)));
    }
}
