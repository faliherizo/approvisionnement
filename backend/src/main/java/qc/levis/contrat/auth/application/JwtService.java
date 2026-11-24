package qc.levis.contrat.auth.application;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Génération et validation des jetons JWT. */
@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String genererToken(String email, List<String> roles, Long tenantId) {
        Date now = new Date();
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles);
        claims.put("tenant", tenantId); // null = superadmin global
        // Identifiant unique : deux sessions ouvertes dans la même seconde ont des jetons distincts, révocables séparément.
        return Jwts.builder()
                .setId(java.util.UUID.randomUUID().toString())
                .setSubject(email)
                .addClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /** Revendication portant l'instant de connexion : elle traverse les renouvellements (durée maximale, ST-01). */
    public static final String DEBUT_SESSION = "debut";

    /**
     * Jeton renouvelé d'une session en cours : même identité, même début de session, validité choisie par la politique
     * de session (ST-01). Les jetons de connexion restent émis par {@link #genererToken(String, List, Long)}.
     */
    public String genererToken(String email, List<String> roles, Long tenantId, java.time.Instant debut, java.time.Duration validite) {
        Date now = new Date();
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles);
        claims.put("tenant", tenantId);
        claims.put(DEBUT_SESSION, debut.getEpochSecond());
        return Jwts.builder()
                .setId(java.util.UUID.randomUUID().toString())
                .setSubject(email)
                .addClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + validite.toMillis()))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parser(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody();
    }

    public long getExpirationMs() { return expirationMs; }
}
