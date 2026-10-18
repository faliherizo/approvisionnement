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
 * État éphémère du flux SAML SP-initiated, transporté dans le {@code RelayState} : le tenant ciblé,
 * signé (HMAC) et de courte durée. À l'inverse d'OIDC (cookie httpOnly), l'ACS SAML reçoit une
 * requête POST inter-site : un cookie {@code SameSite=Lax} n'y serait pas renvoyé. Le RelayState,
 * relayé par l'IdP dans le corps du POST, est donc le porteur adéquat — signé pour empêcher toute
 * altération côté client, sans état serveur.
 */
@Component
public class SamlFlowState {

    private final SecretKey key;
    private final long ttlMs;

    public SamlFlowState(@Value("${app.jwt.secret}") String secret,
                         @Value("${app.saml.flow-ttl-ms:600000}") long ttlMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.ttlMs = ttlMs;
    }

    /** Signe le tenant ciblé pour la durée d'un aller-retour vers l'IdP. */
    public String signer(Long tenantId) {
        Date now = new Date();
        return Jwts.builder()
                .claim("tenant", tenantId)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + ttlMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /** Lit et vérifie le RelayState ; vide si absent, altéré ou expiré. */
    public Optional<Long> lireTenant(String relayState) {
        if (relayState == null || relayState.isBlank()) return Optional.empty();
        try {
            Claims c = Jwts.parserBuilder().setSigningKey(key).build()
                    .parseClaimsJws(relayState).getBody();
            Number tenant = c.get("tenant", Number.class);
            return Optional.ofNullable(tenant == null ? null : tenant.longValue());
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
