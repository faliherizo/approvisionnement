package qc.levis.contrat.auth.infrastructure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.math.BigInteger;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import qc.levis.contrat.integration.application.IntegrationOidcService.ConfigOidc;
import qc.levis.contrat.integration.application.port.PublicationCiblePort.ResultatPublication;

/**
 * Adaptateur du protocole OpenID Connect (JDK {@link HttpClient}) : découverte du fournisseur,
 * échange du code d'autorisation contre un jeton, et <b>validation</b> du jeton d'identité
 * (signature RSA via JWKS, émetteur, audience, expiration, nonce). Aucun secret n'est journalisé.
 */
@Component
public class OidcClient {

    private static final Logger log = LoggerFactory.getLogger(OidcClient.class);

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build();
    private final ObjectMapper json;

    public OidcClient(ObjectMapper json) {
        this.json = json;
    }

    /** Métadonnées de découverte utiles au flux authorization code. */
    public record Decouverte(String issuer, String authorizationEndpoint, String tokenEndpoint,
                             String jwksUri) {}

    /** Résultat de la découverte + validation d'un jeton d'identité. */
    public record Identite(String courriel, String nom) {}

    private String wellKnown(String issuer) {
        String base = issuer.endsWith("/") ? issuer.substring(0, issuer.length() - 1) : issuer;
        return base + "/.well-known/openid-configuration";
    }

    /** Découverte OIDC : lit {@code .well-known/openid-configuration}. Vide en cas d'échec. */
    public Optional<Decouverte> decouvrir(String issuer) {
        if (issuer == null || issuer.isBlank()) return Optional.empty();
        try {
            HttpResponse<String> resp = http.send(
                    HttpRequest.newBuilder(URI.create(wellKnown(issuer))).GET().build(),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (resp.statusCode() / 100 != 2) return Optional.empty();
            JsonNode n = json.readTree(resp.body());
            return Optional.of(new Decouverte(texte(n, "issuer", issuer),
                    texte(n, "authorization_endpoint", null), texte(n, "token_endpoint", null),
                    texte(n, "jwks_uri", null)));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        } catch (Exception e) {
            log.warn("Découverte OIDC impossible pour {} : {}", issuer, e.getMessage());
            return Optional.empty();
        }
    }

    /** Test de configuration : la découverte OIDC répond-elle ? Résultat structuré pour l'admin. */
    public ResultatPublication testerDecouverte(String issuer) {
        return decouvrir(issuer)
                .filter(d -> d.authorizationEndpoint() != null && d.tokenEndpoint() != null)
                .map(d -> new ResultatPublication(true, d.issuer(),
                        "Découverte OIDC réussie (points d'accès trouvés)."))
                .orElse(new ResultatPublication(false, null,
                        "Découverte OIDC impossible : vérifiez l'émetteur (issuer)."));
    }

    /**
     * Échange le code d'autorisation contre les jetons ({@code POST token_endpoint}, form-urlencoded)
     * puis valide le jeton d'identité. Renvoie l'identité (courriel + nom) extraite du jeton validé.
     *
     * @throws OidcException si l'échange échoue ou si le jeton d'identité est invalide.
     */
    public Identite echangerEtValider(Decouverte d, ConfigOidc cfg, String code, String redirectUri,
                                      String codeVerifier, String nonceAttendu) {
        String idToken = echangerCode(d, cfg, code, redirectUri, codeVerifier);
        Claims claims = validerIdToken(idToken, d, cfg, nonceAttendu);
        String courriel = texteClaim(claims, cfg.claimCourriel());
        if (courriel == null || courriel.isBlank()) {
            throw new OidcException("Le jeton d'identité ne contient pas le claim « " + cfg.claimCourriel() + " ».");
        }
        String nom = texteClaim(claims, "name");
        return new Identite(courriel, nom);
    }

    private String echangerCode(Decouverte d, ConfigOidc cfg, String code, String redirectUri,
                                String codeVerifier) {
        String corps = "grant_type=authorization_code"
                + "&code=" + enc(code)
                + "&redirect_uri=" + enc(redirectUri)
                + "&client_id=" + enc(cfg.clientId())
                + "&code_verifier=" + enc(codeVerifier)
                + (cfg.clientSecret() == null || cfg.clientSecret().isBlank()
                        ? "" : "&client_secret=" + enc(cfg.clientSecret()));
        try {
            HttpResponse<String> resp = http.send(
                    HttpRequest.newBuilder(URI.create(d.tokenEndpoint()))
                            .header("Content-Type", "application/x-www-form-urlencoded")
                            .header("Accept", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(corps, StandardCharsets.UTF_8)).build(),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (resp.statusCode() / 100 != 2) {
                throw new OidcException("Échange du code refusé par le fournisseur (" + resp.statusCode() + ").");
            }
            JsonNode n = json.readTree(resp.body());
            String idToken = texte(n, "id_token", null);
            if (idToken == null) throw new OidcException("Réponse du fournisseur sans jeton d'identité.");
            return idToken;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new OidcException("Échange du code interrompu.");
        } catch (OidcException e) {
            throw e;
        } catch (Exception e) {
            throw new OidcException("Échec de l'échange du code : " + e.getMessage());
        }
    }

    private Claims validerIdToken(String idToken, Decouverte d, ConfigOidc cfg, String nonceAttendu) {
        try {
            PublicKey cle = clePublique(d.jwksUri(), kid(idToken));
            Claims claims = Jwts.parserBuilder().setSigningKey(cle).build()
                    .parseClaimsJws(idToken).getBody();
            if (!d.issuer().equals(claims.getIssuer())) {
                throw new OidcException("Émetteur du jeton inattendu.");
            }
            if (!audienceContient(claims, cfg.clientId())) {
                throw new OidcException("Audience du jeton inattendue.");
            }
            if (nonceAttendu != null && !nonceAttendu.equals(claims.get("nonce", String.class))) {
                throw new OidcException("Nonce du jeton invalide.");
            }
            return claims; // jjwt vérifie la signature et l'expiration
        } catch (OidcException e) {
            throw e;
        } catch (Exception e) {
            throw new OidcException("Jeton d'identité invalide : " + e.getMessage());
        }
    }

    /** Reconstitue la clé publique RSA depuis le JWKS pour le {@code kid} du jeton. */
    private PublicKey clePublique(String jwksUri, String kid) throws Exception {
        HttpResponse<String> resp = http.send(
                HttpRequest.newBuilder(URI.create(jwksUri)).GET().build(),
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (resp.statusCode() / 100 != 2) throw new OidcException("JWKS inaccessible.");
        JsonNode cles = json.readTree(resp.body()).path("keys");
        for (JsonNode k : cles) {
            if (kid == null || kid.equals(k.path("kid").asText(null)) || cles.size() == 1) {
                BigInteger n = new BigInteger(1, base64url(k.path("n").asText()));
                BigInteger e = new BigInteger(1, base64url(k.path("e").asText()));
                return KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(n, e));
            }
        }
        throw new OidcException("Aucune clé JWKS ne correspond au jeton.");
    }

    private String kid(String idToken) throws Exception {
        String[] parts = idToken.split("\\.");
        if (parts.length < 2) throw new OidcException("Jeton d'identité malformé.");
        JsonNode entete = json.readTree(base64url(parts[0]));
        return entete.path("kid").asText(null);
    }

    private boolean audienceContient(Claims claims, String clientId) {
        Object aud = claims.get("aud");
        if (aud instanceof String s) return s.equals(clientId);
        if (aud instanceof Iterable<?> it) {
            for (Object o : it) {
                if (clientId.equals(String.valueOf(o))) return true;
            }
        }
        return false;
    }

    private String texteClaim(Claims claims, String nom) {
        Object v = claims.get(nom);
        return v == null ? null : String.valueOf(v);
    }

    private String texte(JsonNode n, String champ, String defaut) {
        JsonNode v = n.get(champ);
        return v == null || v.isNull() ? defaut : v.asText();
    }

    private byte[] base64url(String v) {
        return Base64.getUrlDecoder().decode(v);
    }

    private String enc(String v) {
        return URLEncoder.encode(v == null ? "" : v, StandardCharsets.UTF_8);
    }

    /** Erreur métier du flux OIDC (échange ou validation). */
    public static class OidcException extends RuntimeException {
        public OidcException(String message) { super(message); }
    }
}
