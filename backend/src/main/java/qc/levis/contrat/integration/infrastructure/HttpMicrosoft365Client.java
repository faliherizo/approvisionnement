package qc.levis.contrat.integration.infrastructure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import qc.levis.contrat.commun.domain.Enums;
import qc.levis.contrat.dossier.domain.Dossier;
import qc.levis.contrat.integration.application.port.Microsoft365Client;
import qc.levis.contrat.integration.application.port.PublicationCiblePort;
import qc.levis.contrat.redaction.domain.Document;

/**
 * Implémentation réelle du client Microsoft 365 via l'API Graph (flux <em>client credentials</em>).
 * Utilise le client HTTP du JDK (aucune dépendance SDK). Dépôts SharePoint/OneDrive/Teams par
 * téléversement simple, Outlook par sendMail. Cette classe effectue des appels réseau réels et n'est
 * donc pas couverte par les tests unitaires (exclue de JaCoCo, justification ci-jointe) : sa logique
 * est vérifiée manuellement contre un tenant Microsoft 365 de recette ; l'orchestration et les
 * décisions par tenant sont, elles, testées via {@link GraphPublicationAdapter}.
 */
@Component
public class HttpMicrosoft365Client implements Microsoft365Client {

    private static final String EN_TETE_AUTH = "Authorization";
    private static final String PREFIXE_BEARER = "Bearer ";

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build();
    private final ObjectMapper json;

    /** Base de l'API Graph (surchargée en test/recette pour viser un simulateur). */
    private final String graphBase;
    /** Base de l'émetteur de jetons Entra ID (surchargée pour viser un simulateur). */
    private final String loginBase;

    public HttpMicrosoft365Client(
            ObjectMapper json,
            @Value("${app.m365.graph-base-url:https://graph.microsoft.com/v1.0}") String graphBase,
            @Value("${app.m365.login-base-url:https://login.microsoftonline.com}") String loginBase) {
        this.json = json;
        this.graphBase = graphBase;
        this.loginBase = loginBase;
    }

    @Override
    public PublicationCiblePort.ResultatPublication tester(ConfigM365 config) {
        try {
            jeton(config);
            return new PublicationCiblePort.ResultatPublication(true, null,
                    "Connexion Microsoft 365 réussie (jeton obtenu).");
        } catch (RuntimeException e) {
            return new PublicationCiblePort.ResultatPublication(false, null,
                    "Échec de connexion Microsoft 365 : " + e.getMessage());
        }
    }

    @Override
    public PublicationCiblePort.ResultatPublication deposer(ConfigM365 config, Enums.CiblePublication cible,
                                                            PublicationCiblePort.ContenuPublie contenu) {
        String jeton = jeton(config);
        if (cible == Enums.CiblePublication.OUTLOOK) {
            return envoyerCourriel(config, jeton, contenu);
        }
        String url = urlDepot(config, cible, jeton, chemin(contenu));
        HttpResponse<String> r = envoyer(HttpRequest.newBuilder(URI.create(url))
                .header(EN_TETE_AUTH, PREFIXE_BEARER + jeton)
                .header("Content-Type", contenu.mediaType())
                .PUT(HttpRequest.BodyPublishers.ofByteArray(contenu.octets())).build());
        if (r.statusCode() / 100 != 2) {
            throw new IllegalStateException("Graph " + r.statusCode() + " : " + r.body());
        }
        String webUrl = lire(r.body(), "webUrl");
        // Pour Teams, on renvoie un deep link vers le CANAL dans l'application Teams (et non l'URL
        // SharePoint brute) : le fichier figure dans l'onglet « Partagé » du canal. On évite tout
        // appel Graph fragile ($select, driveId à caractères réservés) → lien toujours constructible.
        if (cible == Enums.CiblePublication.TEAMS
                && config.teamsChannelId() != null && !config.teamsChannelId().isBlank()) {
            String lienTeams = lienCanalTeams(config, jeton);
            if (lienTeams != null) {
                return new PublicationCiblePort.ResultatPublication(true, lienTeams,
                        "Déposé dans le canal Teams (onglet Partagé, " + contenu.octets().length + " octets).");
            }
        }
        return new PublicationCiblePort.ResultatPublication(true, webUrl,
                "Déposé vers " + cible.name() + " (" + contenu.octets().length + " octets).");
    }

    /**
     * Deep link Microsoft Teams vers le <em>canal</em> (ouvre l'application Teams, onglet Partagé où
     * figure le fichier déposé), plutôt que l'URL SharePoint brute. N'utilise que des données de
     * configuration et un appel Graph sûr pour le nom du canal. Renvoie {@code null} si la
     * configuration Teams est incomplète → l'appelant retombe alors sur le webUrl SharePoint.
     */
    private String lienCanalTeams(ConfigM365 config, String jeton) {
        if (config.teamsTeamId() == null || config.teamsChannelId() == null
                || config.msTenantId() == null) {
            return null;
        }
        String threadId = URLDecoder.decode(config.teamsChannelId(), StandardCharsets.UTF_8);
        return "https://teams.microsoft.com/l/channel/" + enc(threadId) + "/" + enc(nomCanalTeams(config, jeton))
                + "?groupId=" + enc(config.teamsTeamId())
                + "&tenantId=" + enc(config.msTenantId());
    }

    /** Nom d'affichage du canal (cosmétique dans le deep link) ; « canal » par défaut si indisponible. */
    private String nomCanalTeams(ConfigM365 config, String jeton) {
        try {
            HttpResponse<String> r = envoyer(HttpRequest.newBuilder(URI.create(graphBase + "/teams/"
                            + config.teamsTeamId() + "/channels/" + config.teamsChannelId()))
                    .header(EN_TETE_AUTH, PREFIXE_BEARER + jeton).GET().build());
            String dn = r.statusCode() / 100 == 2 ? lire(r.body(), "displayName") : null;
            return dn == null || dn.isBlank() ? "canal" : dn;
        } catch (RuntimeException e) {
            return "canal";
        }
    }

    /** URL de téléversement selon la cible ; pour Teams avec canal, on vise le dossier du canal. */
    private String urlDepot(ConfigM365 config, Enums.CiblePublication cible, String jeton, String chemin) {
        if (cible == Enums.CiblePublication.TEAMS
                && config.teamsChannelId() != null && !config.teamsChannelId().isBlank()) {
            return urlDossierCanalTeams(config, jeton, chemin);
        }
        return urlTeleversement(config, cible, chemin);
    }

    private String urlTeleversement(ConfigM365 config, Enums.CiblePublication cible, String chemin) {
        String racine = switch (cible) {
            case SHAREPOINT -> "/sites/" + config.sharepointSiteId() + "/drive";
            case ONEDRIVE -> "/drives/" + config.driveId();
            case TEAMS -> "/groups/" + config.teamsTeamId() + "/drive"; // repli : racine de l'équipe
            default -> throw new IllegalArgumentException("Cible non gérée : " + cible);
        };
        return graphBase + racine + "/root:/" + chemin + ":/content";
    }

    /**
     * Résout le <em>dossier du canal</em> Teams (endpoint {@code filesFolder}) puis renvoie l'URL de
     * dépôt dans ce dossier, pour que le fichier apparaisse dans l'onglet <b>Fichiers</b> du canal
     * (et non seulement à la racine du site SharePoint de l'équipe).
     */
    private String urlDossierCanalTeams(ConfigM365 config, String jeton, String chemin) {
        HttpResponse<String> r = envoyer(HttpRequest.newBuilder(URI.create(graphBase + "/teams/"
                        + config.teamsTeamId() + "/channels/" + config.teamsChannelId() + "/filesFolder"))
                .header(EN_TETE_AUTH, PREFIXE_BEARER + jeton).GET().build());
        if (r.statusCode() / 100 != 2) {
            throw new IllegalStateException("Graph filesFolder " + r.statusCode() + " : " + r.body());
        }
        String driveId = lirePointeur(r.body(), "/parentReference/driveId");
        String itemId = lire(r.body(), "id");
        if (driveId == null || itemId == null) {
            throw new IllegalStateException("Dossier du canal Teams introuvable.");
        }
        return graphBase + "/drives/" + driveId + "/items/" + itemId + ":/" + chemin + ":/content";
    }

    private PublicationCiblePort.ResultatPublication envoyerCourriel(ConfigM365 config, String jeton,
                                                                     PublicationCiblePort.ContenuPublie contenu) {
        String piece = Base64.getEncoder().encodeToString(contenu.octets());
        String corps = "{\"message\":{\"subject\":" + jsonStr("Document : " + contenu.titre())
                + ",\"body\":{\"contentType\":\"Text\",\"content\":\"Document en pièce jointe.\"},"
                + "\"toRecipients\":[{\"emailAddress\":{\"address\":" + jsonStr(config.outlookExpediteur()) + "}}],"
                + "\"attachments\":[{\"@odata.type\":\"#microsoft.graph.fileAttachment\",\"name\":"
                + jsonStr(contenu.nomFichier()) + ",\"contentType\":" + jsonStr(contenu.mediaType())
                + ",\"contentBytes\":" + jsonStr(piece) + "}]}}";
        HttpResponse<String> r = envoyer(HttpRequest.newBuilder(URI.create(graphBase + "/users/"
                        + config.outlookExpediteur() + "/sendMail"))
                .header(EN_TETE_AUTH, PREFIXE_BEARER + jeton)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(corps)).build());
        if (r.statusCode() / 100 != 2) {
            throw new IllegalStateException("Graph sendMail " + r.statusCode() + " : " + r.body());
        }
        return new PublicationCiblePort.ResultatPublication(true, "mailto:" + config.outlookExpediteur(),
                "Courriel Outlook envoyé.");
    }

    private String jeton(ConfigM365 config) {
        String corps = "client_id=" + enc(config.clientId())
                + "&scope=" + enc("https://graph.microsoft.com/.default")
                + "&client_secret=" + enc(config.clientSecretClair())
                + "&grant_type=client_credentials";
        HttpResponse<String> r = envoyer(HttpRequest.newBuilder(URI.create(
                        loginBase + "/" + config.msTenantId() + "/oauth2/v2.0/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(corps)).build());
        if (r.statusCode() / 100 != 2) {
            throw new IllegalStateException("Jeton refusé (" + r.statusCode() + ") : " + r.body());
        }
        String token = lire(r.body(), "access_token");
        if (token == null) throw new IllegalStateException("Jeton absent de la réponse Entra ID.");
        return token;
    }

    private String chemin(PublicationCiblePort.ContenuPublie contenu) {
        String dossier = contenu.destination() == null ? "" : contenu.destination();
        String complet = dossier.isBlank() ? contenu.nomFichier() : dossier + "/" + contenu.nomFichier();
        return Stream.of(complet.split("/")).filter(s -> !s.isBlank())
                .map(s -> URLEncoder.encode(s, StandardCharsets.UTF_8).replace("+", "%20"))
                .collect(Collectors.joining("/"));
    }

    private HttpResponse<String> envoyer(HttpRequest req) {
        try {
            return http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private String lire(String corps, String champ) {
        try {
            JsonNode n = json.readTree(corps).get(champ);
            return n == null ? null : n.asText();
        } catch (Exception e) {
            return null;
        }
    }

    /** Lit une valeur imbriquée via un pointeur JSON (ex. {@code /parentReference/driveId}). */
    private String lirePointeur(String corps, String pointeur) {
        try {
            JsonNode n = json.readTree(corps).at(pointeur);
            return n.isMissingNode() || n.isNull() ? null : n.asText();
        } catch (Exception e) {
            return null;
        }
    }

    private String enc(String v) {
        return URLEncoder.encode(v == null ? "" : v, StandardCharsets.UTF_8);
    }

    private String jsonStr(String v) {
        return "\"" + (v == null ? "" : v.replace("\\", "\\\\").replace("\"", "\\\"")) + "\"";
    }
}
