package qc.levis.contrat.clm.infrastructure;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import qc.levis.contrat.clm.application.ClmProprietes;
import qc.levis.contrat.clm.application.port.PortSalesforce;

/**
 * Adaptateur Salesforce REST (OAuth 2.0 client credentials). Lecture d'une demande par Id ou par numéro (SOQL),
 * retour de statut par PATCH idempotent ; l'identifiant de corrélation accompagne chaque appel.
 */
@Component
@ConditionalOnProperty(name = "app.clm.salesforce.mode", havingValue = "rest")
public class SalesforceRest implements PortSalesforce {

    private static final TypeReference<Map<String, Object>> OBJET = new TypeReference<>() {
    };
    private static final Duration DUREE_JETON = Duration.ofMinutes(30);

    private final ClmProprietes.Salesforce config;
    private final ObjectMapper json;
    private final Clock horloge;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private String jeton;
    private Instant jetonExpireLe = Instant.EPOCH;

    public SalesforceRest(ClmProprietes proprietes, ObjectMapper json, Clock horloge) {
        this.config = proprietes.salesforce();
        this.json = json;
        this.horloge = horloge;
    }

    @Override
    public Optional<Dossier> lireDossier(String identifiantOuNumero) {
        String base = base() + "/sobjects/" + config.objet() + "/";
        HttpResponse<String> r = appeler(HttpRequest.newBuilder(URI.create(base + encoder(identifiantOuNumero))).GET(), null);
        if (r.statusCode() == 404) {
            String soql = "SELECT FIELDS(STANDARD) FROM " + config.objet() + " WHERE Name = '" + identifiantOuNumero.replace("\\", "\\\\").replace("'", "\\'") + "' LIMIT 1";
            r = appeler(HttpRequest.newBuilder(URI.create(base() + "/query?q=" + encoder(soql))).GET(), null);
            List<?> enregistrements = (List<?>) lire(r).getOrDefault("records", List.of());
            if (enregistrements.isEmpty()) {
                return Optional.empty();
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> m = new LinkedHashMap<>((Map<String, Object>) enregistrements.get(0));
            return Optional.of(dossier(m));
        }
        return Optional.of(dossier(lire(r)));
    }

    private Dossier dossier(Map<String, Object> m) {
        m.remove("attributes");
        return new Dossier(String.valueOf(m.get("Id")), String.valueOf(m.getOrDefault("Name", m.get("Id"))), m,
                Instant.now(horloge).truncatedTo(ChronoUnit.MILLIS));
    }

    @Override
    public void publierStatut(Statut s) {
        Map<String, Object> corps = new LinkedHashMap<>();
        corps.put("CLM_Statut__c", s.evenement());
        corps.put("CLM_Reference__c", s.donnees().get("reference"));
        corps.put("CLM_Correlation__c", s.correlationId());
        String url = base() + "/sobjects/" + config.objet() + "/" + encoder(s.sfDossierId());
        HttpResponse<String> r = appeler(HttpRequest.newBuilder(URI.create(url))
                .method("PATCH", HttpRequest.BodyPublishers.ofString(ecrire(corps))), s.correlationId());
        if (r.statusCode() >= 300) {
            throw new Indisponible("Retour de statut refusé par Salesforce (HTTP " + r.statusCode() + ").");
        }
    }

    @Override
    public EtatConnexion tester() {
        try {
            appeler(HttpRequest.newBuilder(URI.create(base() + "/limits")).GET(), null);
            return new EtatConnexion(true, "rest", "Connexion établie (" + config.versionApi() + ")");
        } catch (Indisponible e) {
            return new EtatConnexion(false, "rest", e.getMessage());
        }
    }

    private HttpResponse<String> appeler(HttpRequest.Builder req, String correlation) {
        req.header("Authorization", "Bearer " + jeton()).header("Content-Type", "application/json").timeout(Duration.ofSeconds(20));
        if (correlation != null) {
            req.header("X-Correlation-Id", correlation);
        }
        try {
            HttpResponse<String> r = http.send(req.build(), HttpResponse.BodyHandlers.ofString());
            if (r.statusCode() == 401) {
                jetonExpireLe = Instant.EPOCH;
                throw new Indisponible("Authentification expirée auprès de Salesforce.");
            }
            if (r.statusCode() >= 500) {
                throw new Indisponible("Salesforce indisponible (HTTP " + r.statusCode() + ").");
            }
            return r;
        } catch (IOException e) {
            throw new Indisponible("Salesforce injoignable.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new Indisponible("Appel Salesforce interrompu.", e);
        }
    }

    private synchronized String jeton() {
        if (jeton != null && Instant.now(horloge).isBefore(jetonExpireLe)) {
            return jeton;
        }
        String corps = "grant_type=client_credentials&client_id=" + encoder(config.clientId()) + "&client_secret=" + encoder(config.clientSecret());
        try {
            HttpResponse<String> r = http.send(HttpRequest.newBuilder(URI.create(config.tokenUrl()))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(corps)).timeout(Duration.ofSeconds(20)).build(), HttpResponse.BodyHandlers.ofString());
            if (r.statusCode() != 200) {
                throw new Indisponible("Authentification Salesforce refusée (HTTP " + r.statusCode() + ").");
            }
            jeton = String.valueOf(lire(r).get("access_token"));
            jetonExpireLe = Instant.now(horloge).plus(DUREE_JETON);
            return jeton;
        } catch (IOException e) {
            throw new Indisponible("Serveur d'authentification Salesforce injoignable.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new Indisponible("Authentification Salesforce interrompue.", e);
        }
    }

    private String base() {
        return config.instanceUrl() + "/services/data/" + config.versionApi();
    }

    private Map<String, Object> lire(HttpResponse<String> r) {
        try {
            return json.readValue(r.body(), OBJET);
        } catch (IOException e) {
            throw new Indisponible("Réponse Salesforce illisible.", e);
        }
    }

    private String ecrire(Object o) {
        try {
            return json.writeValueAsString(o);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String encoder(String s) {
        return URLEncoder.encode(s == null ? "" : s, StandardCharsets.UTF_8);
    }
}
