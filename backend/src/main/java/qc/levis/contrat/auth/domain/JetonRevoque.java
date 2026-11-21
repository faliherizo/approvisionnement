package qc.levis.contrat.auth.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Jeton de session révoqué par une déconnexion (OWASP A07) : désigné par l'empreinte SHA-256 du jeton, jamais par le
 * jeton lui-même, et conservé jusqu'à son expiration naturelle, après quoi il ne sert plus à rien.
 */
@Entity
@Table(name = "jeton_revoque")
public class JetonRevoque {

    @Id
    @Column(length = 64)
    private String empreinte;
    @Column(nullable = false) private Instant expireLe;

    protected JetonRevoque() { }

    public JetonRevoque(String empreinte, Instant expireLe) {
        this.empreinte = empreinte;
        this.expireLe = expireLe;
    }

    public String getEmpreinte() { return empreinte; }
    public Instant getExpireLe() { return expireLe; }
}
