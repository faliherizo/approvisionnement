package qc.levis.contrat.interop.domain;

import jakarta.persistence.*;
import java.time.Instant;

/** Jeton d'accès opaque émis à un client d'API (stocké haché). */
@Entity
@Table(name = "api_token")
public class ApiToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "client_id", nullable = false, length = 80)
    private String clientId;

    @Column(name = "token_hash", nullable = false, unique = true, length = 100)
    private String tokenHash;

    @Column(name = "expire_le", nullable = false)
    private Instant expireLe;

    public ApiToken() {}

    public ApiToken(Long tenantId, String clientId, String tokenHash, Instant expireLe) {
        this.tenantId = tenantId;
        this.clientId = clientId;
        this.tokenHash = tokenHash;
        this.expireLe = expireLe;
    }

    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public String getClientId() { return clientId; }
    public String getTokenHash() { return tokenHash; }
    public Instant getExpireLe() { return expireLe; }
}
