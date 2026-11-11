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
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import qc.levis.contrat.clm.application.ConnexionsSalesforce;
import qc.levis.contrat.clm.application.port.PortSalesforce;

/**
 * Adaptateur Salesforce REST (OAuth 2.0 client credentials). Lecture d'une demande par Id ou par numéro (SOQL),
 * retour de statut par PATCH idempotent ; l'identifiant de corrélation accompagne chaque appel. Chaque appel emploie la
 * connexion de l'organisation en cours ; un jeton ne sert qu'à la connexion exacte qui l'a obtenu.
 */
@Component
public class SalesforceRest implements PortSalesforce {

    private static final TypeReference<Map<String, Object>> OBJET = new TypeReference<>() {
    };
    private static final Duration DUREE_JETON = Duration.ofMinutes(30);

    private record Jeton(ConnexionsSalesforce.Connexion connexion, String valeur, Instant expireLe) {
    }

    private final Supplier<ConnexionsSalesforce.Connexion> connexions;
    private final ObjectMapper json;
    private final Clock horloge;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final Map<String, Jeton> jetons = new ConcurrentHashMap<>();

    @Autowired
    public SalesforceRest(ConnexionsSalesforce connexions, ObjectMapper json, Clock horloge) {
        this(connexions::courante, json, horloge);
    }

    SalesforceRest(Supplier<ConnexionsSalesforce.Connexion> connexions, ObjectMapper json, Clock horloge) {
        this.connexions = connexions;
        this.json = json;
        this.horloge = horloge;
    }

    /**
     * Lecture par identifiant ; à défaut, recherche par numéro (SOQL) puis relecture par identifiant. La relecture rend tous
     * les champs, personnalisés compris (« …__c ») : {@code FIELDS(STANDARD)} les omettrait et le préremplissage les perdrait.
     */
    @Override
    public Optional<Dossier> lireDossier(String identifiantOuNumero) {
        ConnexionsSalesforce.Connexion c = connexions.get();
        Optional<Dossier> parId = parIdentifiant(c, identifiantOuNumero);
        if (parId.isPresent()) {
            return parId;
        }
        String soql = "SELECT Id FROM " + c.objet() + " WHERE Name = '" + identifiantOuNumero.replace("\\", "\\\\").replace("'", "\\'") + "' LIMIT 1";
        HttpResponse<String> r = appeler(c, HttpRequest.newBuilder(URI.create(base(c) + "/query?q=" + encoder(soql))).GET(), null);
        List<?> enregistrements = (List<?>) lire(r).getOrDefault("records", List.of());
        if (enregistrements.isEmpty()) {
            return Optional.empty();
        }
        return parIdentifiant(c, String.valueOf(((Map<?, ?>) enregistrements.get(0)).get("Id")));
    }

    private Optional<Dossier> parIdentifiant(ConnexionsSalesforce.Connexion c, String identifiant) {
        String url = base(c) + "/sobjects/" + c.objet() + "/" + encoder(identifiant);
        HttpResponse<String> r = appeler(c, HttpRequest.newBuilder(URI.create(url)).GET(), null);
        return r.statusCode() == 404 ? Optional.empty() : Optional.of(dossier(new LinkedHashMap<>(lire(r))));
    }

    private Dossier dossier(Map<String, Object> m) {
        m.remove("attributes");
        return new Dossier(String.valueOf(m.get("Id")), String.valueOf(m.getOrDefault("Name", m.get("Id"))), m,
                Instant.now(horloge).truncatedTo(ChronoUnit.MILLIS));
    }

    @Override
    public void publierStatut(Statut s) {
        ConnexionsSalesforce.Connexion c = connexions.get();
        Map<String, Object> corps = new LinkedHashMap<>();
        corps.put(c.champs().statut(), s.evenement());
        corps.put(c.champs().reference(), s.donnees().get("reference"));
        corps.put(c.champs().correlation(), s.correlationId());
        String url = base(c) + "/sobjects/" + c.objet() + "/" + encoder(s.sfDossierId());
        HttpResponse<String> r = appeler(c, HttpRequest.newBuilder(URI.create(url))
                .method("PATCH", HttpRequest.BodyPublishers.ofString(ecrire(corps))), s.correlationId());
        if (r.statusCode() >= 300) {
            throw new Indisponible("Retour de statut refusé par Salesforce (HTTP " + r.statusCode() + ").");
        }
    }

    @Override
    public EtatConnexion tester() {
        ConnexionsSalesforce.Connexion c = connexions.get();
        try {
            appeler(c, HttpRequest.newBuilder(URI.create(base(c) + "/limits")).GET(), null);
            return new EtatConnexion(true, "rest", "Connexion établie (" + c.versionApi() + ")");
        } catch (Indisponible e) {
            return new EtatConnexion(false, "rest", e.getMessage());
        }
    }

    private HttpResponse<String> appeler(ConnexionsSalesforce.Connexion c, HttpRequest.Builder req, String correlation) {
        req.header("Authorization", "Bearer " + jeton(c)).header("Content-Type", "application/json").timeout(Duration.ofSeconds(20));
        if (correlation != null) {
            req.header("X-Correlation-Id", correlation);
        }
        try {
            HttpResponse<String> r = http.send(req.build(), HttpResponse.BodyHandlers.ofString());
            if (r.statusCode() == 401) {
                jetons.remove(cle(c));
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

    /** Un jeton par propriétaire de connexion ; une connexion modifiée (secret, client, URL) en exige un nouveau. */
    private synchronized String jeton(ConnexionsSalesforce.Connexion c) {
        Jeton j = jetons.get(cle(c));
        if (j != null && j.connexion().equals(c) && Instant.now(horloge).isBefore(j.expireLe())) {
            return j.valeur();
        }
        String corps = "grant_type=client_credentials&client_id=" + encoder(c.clientId()) + "&client_secret=" + encoder(c.clientSecret());
        try {
            HttpResponse<String> r = http.send(HttpRequest.newBuilder(URI.create(c.tokenUrl()))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(corps)).timeout(Duration.ofSeconds(20)).build(), HttpResponse.BodyHandlers.ofString());
            if (r.statusCode() != 200) {
                throw new Indisponible("Authentification Salesforce refusée (HTTP " + r.statusCode() + ").");
            }
            String valeur = String.valueOf(lire(r).get("access_token"));
            jetons.put(cle(c), new Jeton(c, valeur, Instant.now(horloge).plus(DUREE_JETON)));
            return valeur;
        } catch (IOException e) {
            throw new Indisponible("Serveur d'authentification Salesforce injoignable.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new Indisponible("Authentification Salesforce interrompue.", e);
        }
    }

    private static String cle(ConnexionsSalesforce.Connexion c) {
        return c.proprietaire() == null ? "plateforme" : c.proprietaire().toString();
    }

    private static String base(ConnexionsSalesforce.Connexion c) {
        return c.instanceUrl() + "/services/data/" + c.versionApi();
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
