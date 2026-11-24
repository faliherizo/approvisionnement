package qc.levis.contrat.notification.infrastructure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import qc.levis.contrat.commun.infrastructure.reseau.GardeSortante;
import qc.levis.contrat.notification.application.port.CanalNotification;

/**
 * Canal SMS par Twilio (API REST publiée : {@code POST /2010-04-01/Accounts/{AccountSid}/Messages.json}, formulaire
 * encodé, authentification Basic). Même compte que les autres applications Vina : TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN,
 * TWILIO_FROM, lus dans l'environnement seulement. Le numéro du destinataire n'est jamais journalisé en entier (Loi 25) ;
 * un échec rend « ECHEC » avec le code d'erreur Twilio, que la messagerie consigne et relance.
 */
@Component
@ConditionalOnProperty(name = "app.notification.sms.mode", havingValue = "twilio")
public class SmsTwilio implements CanalNotification {

    private static final Logger log = LoggerFactory.getLogger(SmsTwilio.class);
    private static final ObjectMapper JSON = new ObjectMapper();

    private final String base;
    private final String compte;
    private final String autorisation;
    private final String expediteur;
    private final GardeSortante garde;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    public SmsTwilio(@Value("${app.notification.sms.twilio.url}") String url,
                     @Value("${app.notification.sms.twilio.account-sid}") String compte,
                     @Value("${app.notification.sms.twilio.auth-token}") String jeton,
                     @Value("${app.notification.sms.twilio.from}") String expediteur,
                     GardeSortante garde) {
        if (blanc(compte) || blanc(jeton) || blanc(expediteur)) {
            throw new IllegalStateException("Canal SMS Twilio : TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN et TWILIO_FROM sont requis.");
        }
        this.base = url.replaceFirst("/$", "");
        this.compte = compte;
        this.autorisation = "Basic " + Base64.getEncoder().encodeToString((compte + ":" + jeton).getBytes(StandardCharsets.UTF_8));
        this.expediteur = expediteur;
        this.garde = garde;
    }

    @Override
    public String canal() {
        return "SMS";
    }

    @Override
    public String envoyer(String destinataire, String sujet, String corps) {
        Map<String, String> formulaire = new LinkedHashMap<>();
        formulaire.put("To", destinataire);
        formulaire.put("From", expediteur);
        formulaire.put("Body", corps);
        try {
            HttpRequest requete = HttpRequest.newBuilder(garde.exiger(base + "/2010-04-01/Accounts/" + encoder(compte) + "/Messages.json"))
                    .timeout(Duration.ofSeconds(20))
                    .header("Authorization", autorisation)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(formulaire.entrySet().stream()
                            .map(e -> encoder(e.getKey()) + "=" + encoder(e.getValue())).collect(Collectors.joining("&"))))
                    .build();
            HttpResponse<String> r = http.send(requete, HttpResponse.BodyHandlers.ofString());
            if (r.statusCode() / 100 == 2) {
                return "ENVOYE";
            }
            log.warn("SMS non remis à {} : Twilio {} (code {})", masquer(destinataire), r.statusCode(), codeErreur(r.body()));
            return "ECHEC";
        } catch (IOException | GardeSortante.DestinationRefusee e) {
            log.warn("SMS non remis à {} : {}", masquer(destinataire), e.getMessage());
            return "ECHEC";
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "ECHEC";
        }
    }

    /** Deux derniers chiffres seulement : assez pour l'exploitant, rien d'identifiant. */
    static String masquer(String numero) {
        return numero == null || numero.length() < 2 ? "?" : "…" + numero.substring(numero.length() - 2);
    }

    static String codeErreur(String corps) {
        try {
            JsonNode n = JSON.readTree(corps);
            return n.path("code").asText("?");
        } catch (IOException | RuntimeException illisible) {
            return "?";
        }
    }

    private static String encoder(String v) {
        return URLEncoder.encode(v == null ? "" : v, StandardCharsets.UTF_8);
    }

    private static boolean blanc(String v) {
        return v == null || v.isBlank();
    }
}
