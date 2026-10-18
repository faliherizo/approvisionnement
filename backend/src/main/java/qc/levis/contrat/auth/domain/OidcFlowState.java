package qc.levis.contrat.auth.domain;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * État éphémère du flux OIDC (authorization code + PKCE), transporté dans un cookie httpOnly
 * <b>signé</b> et de courte durée : {@code state} (anti-CSRF), {@code nonce} (anti-rejeu), le
 * {@code code_verifier} PKCE, le tenant et l'URI de redirection. Signé (HMAC) pour empêcher toute
 * altération côté client ; aucun état serveur n'est nécessaire.
 */
@Component
public class OidcFlowState {

    private final SecretKey key;
    private final long ttlMs;

    public OidcFlowState(@Value("${app.jwt.secret}") String secret,
                         @Value("${app.oidc.flow-ttl-ms:600000}") long ttlMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.ttlMs = ttlMs;
    }

    /** Données du flux en cours (le temps d'un aller-retour vers le fournisseur d'identité). */
    public record Etat(String state, String nonce, String codeVerifier, Long tenantId, String redirectUri) {}

    public String signer(Etat etat) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(etat.state())
                .claim("nonce", etat.nonce())
                .claim("cv", etat.codeVerifier())
                .claim("tenant", etat.tenantId())
                .claim("redirect", etat.redirectUri())
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + ttlMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /** Lit et vérifie le cookie de flux ; vide si absent, altéré ou expiré. */
    public Optional<Etat> lire(String jeton) {
        if (jeton == null || jeton.isBlank()) return Optional.empty();
        try {
            Claims c = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(jeton).getBody();
            Number tenant = c.get("tenant", Number.class);
            return Optional.of(new Etat(c.getSubject(), c.get("nonce", String.class),
                    c.get("cv", String.class), tenant == null ? null : tenant.longValue(),
                    c.get("redirect", String.class)));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public long getTtlMs() { return ttlMs; }
}
