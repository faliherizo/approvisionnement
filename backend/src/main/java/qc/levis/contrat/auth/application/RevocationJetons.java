package qc.levis.contrat.auth.application;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.util.HexFormat;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.auth.application.repository.JetonRevoqueRepository;
import qc.levis.contrat.auth.domain.JetonRevoque;

/**
 * Révocation des jetons de session à la déconnexion : un jeton volé puis rejoué après la déconnexion est refusé, alors
 * qu'un jeton sans état resterait valide jusqu'à son expiration. Les jetons révoqués expirés sont purgés au passage.
 */
@Service
public class RevocationJetons {

    private final JetonRevoqueRepository revoques;
    private final JwtService jwt;
    private final Clock horloge;

    public RevocationJetons(JetonRevoqueRepository revoques, JwtService jwt, Clock horloge) {
        this.revoques = revoques;
        this.jwt = jwt;
        this.horloge = horloge;
    }

    /** Un jeton illisible ou déjà expiré n'a rien à révoquer. */
    @Transactional
    public void revoquer(String jeton) {
        if (jeton == null || jeton.isBlank()) return;
        Instant expire;
        try {
            expire = jwt.parser(jeton).getExpiration().toInstant();
        } catch (RuntimeException illisible) {
            return;
        }
        revoques.purger(horloge.instant());
        revoques.save(new JetonRevoque(empreinte(jeton), expire));
    }

    public boolean estRevoque(String jeton) {
        return revoques.existsById(empreinte(jeton));
    }

    static String empreinte(String jeton) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(jeton.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponible", e);
        }
    }
}
