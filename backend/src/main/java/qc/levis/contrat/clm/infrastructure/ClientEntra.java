package qc.levis.contrat.clm.infrastructure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Appels JSON authentifiés par Microsoft Entra ID (flux client credentials), pour les adaptateurs Azure Key Vault (ADR-037)
 * et Microsoft Purview (ADR-036). Les jetons se gardent en mémoire jusqu'à une minute avant leur expiration ; aucun secret
 * ni jeton n'est journalisé. Une réponse hors 2xx lève {@link Refus}, qui porte le statut sans le corps.
 */
public class ClientEntra {

    /** Identité d'application : émetteur, locataire Entra ID, client et secret, portée demandée. */
    public record Identite(String loginUrl, String tenantId, String clientId, String clientSecret, String portee) {
    }

    /** Réponse refusée par le service distant. */
    public static final class Refus extends RuntimeException {
        private final int statut;

        Refus(String quoi, int statut) {
            super(quoi + " : HTTP " + statut);
            this.statut = statut;
        }

        public int statut() {
            return statut;
        }
    }

    private record Jeton(String valeur, Instant expire) {
    }

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build();
    private final ObjectMapper json;
    private final Clock horloge;
    private final Map<Identite, Jeton> jetons = new ConcurrentHashMap<>();

    public ClientEntra(ObjectMapper json, Clock horloge) {
        this.json = json;
        this.horloge = horloge;
    }

    public JsonNode get(Identite qui, String url) {
        return envoyer(qui, HttpRequest.newBuilder(URI.create(url)).GET(), url);
    }

    public JsonNode post(Identite qui, String url, Object corps) {
        return envoyer(qui, HttpRequest.newBuilder(URI.create(url)).header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(ecrire(corps))), url);
    }

    public JsonNode patch(Identite qui, String url, Object corps) {
        return envoyer(qui, HttpRequest.newBuilder(URI.create(url)).header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(ecrire(corps))), url);
    }

    private JsonNode envoyer(Identite qui, HttpRequest.Builder requete, String url) {
        HttpResponse<String> r = executer(requete.header("Authorization", "Bearer " + jeton(qui)).timeout(Duration.ofSeconds(30)).build());
        if (r.statusCode() / 100 != 2) {
            throw new Refus(URI.create(url).getPath(), r.statusCode());
        }
        return lire(r.body());
    }

    String jeton(Identite qui) {
        Jeton j = jetons.get(qui);
        if (j != null && j.expire().isAfter(Instant.now(horloge))) {
            return j.valeur();
        }
        Jeton nouveau = demander(qui);
        jetons.put(qui, nouveau);
        return nouveau.valeur();
    }

    private Jeton demander(Identite qui) {
        String corps = "grant_type=client_credentials&client_id=" + enc(qui.clientId()) + "&client_secret=" + enc(qui.clientSecret())
                + "&scope=" + enc(qui.portee());
        HttpResponse<String> r = executer(HttpRequest.newBuilder(URI.create(qui.loginUrl() + "/" + qui.tenantId() + "/oauth2/v2.0/token"))
                .header("Content-Type", "application/x-www-form-urlencoded").POST(HttpRequest.BodyPublishers.ofString(corps)).build());
        if (r.statusCode() / 100 != 2) {
            throw new Refus("jeton Entra ID", r.statusCode());
        }
        JsonNode n = lire(r.body());
        long duree = Math.max(0, n.path("expires_in").asLong(3600) - 60);
        return new Jeton(n.path("access_token").asText(), Instant.now(horloge).plusSeconds(duree));
    }

    private HttpResponse<String> executer(HttpRequest requete) {
        try {
            return http.send(requete, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Appel interrompu.", e);
        } catch (java.io.IOException e) {
            throw new IllegalStateException("Service injoignable : " + e.getClass().getSimpleName() + ".", e);
        }
    }

    private JsonNode lire(String corps) {
        try {
            return corps == null || corps.isBlank() ? json.createObjectNode() : json.readTree(corps);
        } catch (java.io.IOException e) {
            throw new IllegalStateException("Réponse illisible.", e);
        }
    }

    private String ecrire(Object corps) {
        try {
            return json.writeValueAsString(corps);
        } catch (java.io.IOException e) {
            throw new IllegalStateException("Requête illisible.", e);
        }
    }

    private static String enc(String v) {
        return URLEncoder.encode(v == null ? "" : v, StandardCharsets.UTF_8);
    }
}
