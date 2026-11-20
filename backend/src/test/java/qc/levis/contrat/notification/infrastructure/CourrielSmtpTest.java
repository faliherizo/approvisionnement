package qc.levis.contrat.notification.infrastructure;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;

/** Canal COURRIEL par SMTP, contre un vrai serveur SMTP (Mailpit, démarré par Testcontainers). */
@Testcontainers(disabledWithoutDocker = true)
class CourrielSmtpTest {

    private static final GenericContainer<?> MAILPIT = new GenericContainer<>("axllent/mailpit:v1.21.8")
            .withExposedPorts(1025, 8025).waitingFor(Wait.forHttp("/api/v1/info").forPort(8025)).withStartupTimeout(Duration.ofMinutes(2));

    @BeforeAll
    static void demarrer() {
        MAILPIT.start();
    }

    @AfterAll
    static void arreter() {
        MAILPIT.stop();
    }

    private static JavaMailSenderImpl relais(int port) {
        JavaMailSenderImpl m = new JavaMailSenderImpl();
        m.setHost(MAILPIT.getHost());
        m.setPort(port);
        m.getJavaMailProperties().put("mail.smtp.connectiontimeout", "3000");
        return m;
    }

    @Test
    void le_message_est_remis_au_serveur_smtp_avec_l_expediteur_du_domaine() throws Exception {
        CourrielSmtp c = new CourrielSmtp(relais(MAILPIT.getMappedPort(1025)), "no-reply@contrats.example");
        assertEquals("COURRIEL", c.canal());
        assertEquals("ENVOYE", c.envoyer("marie@agrinord.example", "Invitation à signer — IQ-2026-0001",
                "Lien : https://exemple/portail/abc\nÀ bientôt."));
        String boite = HttpClient.newHttpClient().send(HttpRequest.newBuilder(URI.create("http://" + MAILPIT.getHost() + ":"
                + MAILPIT.getMappedPort(8025) + "/api/v1/messages")).build(), HttpResponse.BodyHandlers.ofString()).body();
        assertTrue(boite.contains("Invitation à signer — IQ-2026-0001"), boite);
        assertTrue(boite.contains("marie@agrinord.example") && boite.contains("no-reply@contrats.example"), boite);
    }

    @Test
    void un_relais_injoignable_ou_une_adresse_invalide_rend_echec_sans_exception() {
        assertEquals("ECHEC", new CourrielSmtp(relais(1), "no-reply@contrats.example").envoyer("marie@agrinord.example", "Sujet", "Corps"));
        assertEquals("ECHEC", new CourrielSmtp(relais(MAILPIT.getMappedPort(1025)), "no-reply@contrats.example")
                .envoyer("pas une adresse @@", "Sujet", "Corps"), "adresse invalide");
        assertEquals("agrinord.example", CourrielSmtp.domaine("marie@agrinord.example"));
        assertEquals("?", CourrielSmtp.domaine("sans-arobase"));
        assertEquals("?", CourrielSmtp.domaine(null));
    }
}
