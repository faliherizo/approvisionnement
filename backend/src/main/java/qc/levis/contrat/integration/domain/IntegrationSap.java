package qc.levis.contrat.integration.domain;

import jakarta.persistence.*;

/**
 * Configuration d'intégration SAP propre à un tenant (chaque organisation a son propre SAP : URL du
 * service OData, authentification). Saisie depuis l'espace admin (Paramètres). Tant qu'elle est
 * absente ou inactive, la transmission de référence financière reste simulée. Les secrets
 * (mot de passe, clé API) sont stockés <strong>chiffrés</strong>.
 */
@Entity
@Table(name = "integration_sap")
public class IntegrationSap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", unique = true)
    private Long tenantId;

    /** Active les dépôts réels vers SAP pour ce tenant (sinon simulation). */
    @Column(nullable = false)
    private boolean actif = false;

    @Column(name = "base_url", length = 400) private String baseUrl;
    @Column(name = "service_path", length = 400) private String servicePath;

    /** Mode d'authentification : {@code basic} ou {@code apikey}. */
    private String auth = "basic";

    private String username;

    /** Mot de passe (auth basic) chiffré. Jamais exposé en clair. */
    @Column(name = "password_chiffre", length = 1000) private String passwordChiffre;

    /** Clé API (auth apikey) chiffrée. Jamais exposée en clair. */
    @Column(name = "api_key_chiffre", length = 1000) private String apiKeyChiffre;

    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String v) { this.baseUrl = v; }
    public String getServicePath() { return servicePath; }
    public void setServicePath(String v) { this.servicePath = v; }
    public String getAuth() { return auth; }
    public void setAuth(String v) { this.auth = v; }
    public String getUsername() { return username; }
    public void setUsername(String v) { this.username = v; }
    public String getPasswordChiffre() { return passwordChiffre; }
    public void setPasswordChiffre(String v) { this.passwordChiffre = v; }
    public String getApiKeyChiffre() { return apiKeyChiffre; }
    public void setApiKeyChiffre(String v) { this.apiKeyChiffre = v; }

    /** Vrai si la configuration minimale (URL + service) est présente. */
    public boolean estConfigure() {
        return baseUrl != null && !baseUrl.isBlank()
                && servicePath != null && !servicePath.isBlank();
    }
}
