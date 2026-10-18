package qc.levis.contrat.integration;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Fournisseur OpenID Connect simulé en mémoire (serveur HTTP du JDK, aucune dépendance externe).
 * Reproduit la surface utile pour valider le flux authorization code + PKCE de bout en bout :
 *
 * <ul>
 *   <li>découverte : {@code GET /.well-known/openid-configuration} ;</li>
 *   <li>autorisation : {@code GET /authorize} (capture le nonce, redirige vers redirect_uri) ;</li>
 *   <li>jetons : {@code POST /token} (renvoie un id_token signé RS256) ;</li>
 *   <li>clés publiques : {@code GET /jwks} (clé RSA au format JWKS).</li>
 * </ul>
 *
 * Sert uniquement aux tests.
 */
final class FakeOidcServer {

    static final String KID = "test-key-1";

    private final HttpServer server;
    private final KeyPair paire;

    // Paramètres du jeton émis (modifiables par test).
    String clientId = "client-oidc";
    String courriel = "agent.gouv@ville.ca";
    String nom = "Agent Gouvernemental";
    String issuerOverride;              // pour simuler un émetteur incohérent (sinon = base URL)
    boolean decouverteIndisponible = false;

    // État capturé lors de l'autorisation.
    volatile String dernierNonce;
    volatile String derniereMethodeToken;

    FakeOidcServer() {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            this.paire = kpg.generateKeyPair();
            this.server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        } catch (Exception e) {
            throw new IllegalStateException("Impossible de démarrer le simulateur OIDC", e);
        }
        server.createContext("/", this::traiter);
    }

    void demarrer() { server.start(); }

    void arreter() { server.stop(0); }

    int port() { return server.getAddress().getPort(); }

    String issuer() { return "http://127.0.0.1:" + port(); }

    private void traiter(HttpExchange ex) throws IOException {
        String chemin = ex.getRequestURI().getPath();
        if (chemin.endsWith("/.well-known/openid-configuration")) {
            if (decouverteIndisponible) { repondre(ex, 404, ""); return; }
            repondre(ex, 200, decouverte());
        } else if (chemin.endsWith("/authorize")) {
            autoriser(ex);
        } else if (chemin.endsWith("/jwks")) {
            repondre(ex, 200, jwks());
        } else if (chemin.endsWith("/token")) {
            derniereMethodeToken = ex.getRequestMethod();
            lire(ex); // vide le corps
            repondre(ex, 200, "{\"access_token\":\"AT\",\"token_type\":\"Bearer\",\"expires_in\":3600,"
                    + "\"id_token\":\"" + idToken() + "\"}");
        } else {
            repondre(ex, 404, "");
        }
    }

    private void autoriser(HttpExchange ex) throws IOException {
        Map<String, String> params = params(ex.getRequestURI());
        dernierNonce = params.get("nonce");
        String redirect = params.getOrDefault("redirect_uri", "");
        String state = params.getOrDefault("state", "");
        String location = redirect + (redirect.contains("?") ? "&" : "?")
                + "code=FAKECODE&state=" + state;
        ex.getResponseHeaders().add("Location", location);
        repondre(ex, 302, "");
    }

    private String decouverte() {
        String base = issuer();
        return "{\"issuer\":\"" + (issuerOverride != null ? issuerOverride : base) + "\","
                + "\"authorization_endpoint\":\"" + base + "/authorize\","
                + "\"token_endpoint\":\"" + base + "/token\","
                + "\"jwks_uri\":\"" + base + "/jwks\"}";
    }

    private String idToken() {
        Date now = new Date();
        return Jwts.builder()
                .setHeaderParam("kid", KID)
                .setIssuer(issuerOverride != null ? issuerOverride : issuer())
                .setSubject("oidc-subject-1")
                .setAudience(clientId)
                .claim("nonce", dernierNonce)
                .claim("email", courriel)
                .claim("name", nom)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + 300_000))
                .signWith(paire.getPrivate(), SignatureAlgorithm.RS256)
                .compact();
    }

    private String jwks() {
        RSAPublicKey pub = (RSAPublicKey) paire.getPublic();
        String n = b64(pub.getModulus());
        String e = b64(pub.getPublicExponent());
        return "{\"keys\":[{\"kty\":\"RSA\",\"use\":\"sig\",\"kid\":\"" + KID + "\","
                + "\"n\":\"" + n + "\",\"e\":\"" + e + "\"}]}";
    }

    private String b64(BigInteger v) {
        byte[] b = v.toByteArray();
        if (b.length > 1 && b[0] == 0) {
            byte[] sansSigne = new byte[b.length - 1];
            System.arraycopy(b, 1, sansSigne, 0, sansSigne.length);
            b = sansSigne;
        }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }

    private Map<String, String> params(URI uri) {
        String q = uri.getRawQuery();
        if (q == null || q.isBlank()) return Map.of();
        return java.util.Arrays.stream(q.split("&"))
                .map(p -> p.split("=", 2))
                .collect(Collectors.toMap(
                        p -> java.net.URLDecoder.decode(p[0], StandardCharsets.UTF_8),
                        p -> p.length > 1 ? java.net.URLDecoder.decode(p[1], StandardCharsets.UTF_8) : "",
                        (a, b) -> a));
    }

    private String lire(HttpExchange ex) throws IOException {
        return new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    private void repondre(HttpExchange ex, int code, String corps) throws IOException {
        byte[] octets = corps.getBytes(StandardCharsets.UTF_8);
        if (!corps.isEmpty()) ex.getResponseHeaders().add("Content-Type", "application/json");
        ex.sendResponseHeaders(code, octets.length == 0 ? -1 : octets.length);
        if (octets.length > 0) {
            try (OutputStream os = ex.getResponseBody()) {
                os.write(octets);
            }
        }
        ex.close();
    }
}
