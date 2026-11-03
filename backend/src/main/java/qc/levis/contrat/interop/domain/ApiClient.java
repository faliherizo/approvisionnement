package qc.levis.contrat.interop.domain;

import jakarta.persistence.*;

/** Client d'API externe (intégrateur CHU). Le secret n'est stocké que haché. Tenant-scopé. */
@Entity
@Table(name = "api_client")
public class ApiClient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "client_id", nullable = false, unique = true, length = 80)
    private String clientId;

    @Column(name = "secret_hash", nullable = false, length = 100)
    private String secretHash;

    @Column(nullable = false, length = 200)
    private String nom;

    @Column(nullable = false)
    private boolean actif = true;

    @Column(length = 500)
    private String scopes;

    public ApiClient() {}

    public ApiClient(Long tenantId, String clientId, String secretHash, String nom, String scopes) {
        this.tenantId = tenantId;
        this.clientId = clientId;
        this.secretHash = secretHash;
        this.nom = nom;
        this.scopes = scopes;
    }

    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public String getClientId() { return clientId; }
    public String getSecretHash() { return secretHash; }
    public String getNom() { return nom; }
    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }
    public String getScopes() { return scopes; }
}
