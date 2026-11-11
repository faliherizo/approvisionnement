package qc.levis.contrat.clm.infrastructure;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.springframework.stereotype.Component;
import qc.levis.contrat.clm.application.port.PortRappel;

/**
 * Rappel HTTP POST vers le système externe (Workday via son intergiciel, Salesforce, autre). En-têtes :
 * {@code X-CLM-Signature} (HMAC-SHA256 du corps) et {@code Idempotency-Key}. Toute réponse hors 2xx est un refus.
 */
@Component
public class RappelHttp implements PortRappel {

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NEVER).build();

    @Override
    public void envoyer(String url, String corpsJson, String signature, String cleIdempotence) {
        HttpRequest requete = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/json; charset=utf-8")
                .header("X-CLM-Signature", signature)
                .header("Idempotency-Key", cleIdempotence)
                .POST(HttpRequest.BodyPublishers.ofString(corpsJson)).build();
        try {
            int statut = http.send(requete, HttpResponse.BodyHandlers.discarding()).statusCode();
            if (statut < 200 || statut >= 300) {
                throw new Refuse("Rappel refusé par le destinataire (HTTP " + statut + ").");
            }
        } catch (IOException e) {
            throw new Refuse("Destinataire du rappel injoignable.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new Refuse("Envoi du rappel interrompu.", e);
        }
    }
}
