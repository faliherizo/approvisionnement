package qc.levis.contrat.integration.domain;

import jakarta.persistence.*;

/**
 * Configuration d'authentification OpenID Connect propre à un tenant (Microsoft Entra ID ou tout
 * fournisseur OIDC). Saisie et gérée depuis l'espace d'administration du tenant, pour que chaque
 * organisation branche son propre annuaire gouvernemental. Tant qu'elle est absente ou inactive,
 * la page de connexion n'affiche que l'authentification locale. Le secret client est stocké
 * <strong>chiffré</strong> et n'est jamais exposé en clair via l'API.
 */
@Entity
@Table(name = "integration_oidc")
public class IntegrationOidc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", unique = true)
    private Long tenantId;

    /** Active la connexion OIDC pour ce tenant (sinon la page de connexion l'ignore). */
    @Column(nullable = false)
    private boolean actif = false;

    /** Autorité / URL de découverte (ex. https://login.microsoftonline.com/&lt;tenant&gt;/v2.0). */
    @Column(length = 500) private String issuer;

    @Column(name = "client_id", length = 300) private String clientId;

    /** Secret client chiffré (AES). Jamais exposé en clair via l'API. */
    @Column(name = "client_secret_chiffre", length = 1000) private String clientSecretChiffre;

    @Column(length = 300) private String scopes = "openid profile email";

    /** Libellé du bouton affiché sur la page de connexion (ex. « Connexion gouvernementale »). */
    @Column(name = "bouton_libelle", length = 200) private String boutonLibelle = "Connexion gouvernementale";

    /** Nom du claim portant le courriel dans le jeton d'identité (défaut « email »). */
    @Column(name = "claim_courriel", length = 100) private String claimCourriel = "email";

    /** Provisionne automatiquement un compte au premier accès si le courriel est inconnu. */
    @Column(name = "auto_provision", nullable = false)
    private boolean autoProvision = true;

    /** Rôle attribué aux comptes auto-provisionnés (défaut REDACTEUR). */
    @Column(name = "role_par_defaut", length = 30) private String roleParDefaut = "REDACTEUR";

    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }
    public String getIssuer() { return issuer; }
    public void setIssuer(String v) { this.issuer = v; }
    public String getClientId() { return clientId; }
    public void setClientId(String v) { this.clientId = v; }
    public String getClientSecretChiffre() { return clientSecretChiffre; }
    public void setClientSecretChiffre(String v) { this.clientSecretChiffre = v; }
    public String getScopes() { return scopes; }
    public void setScopes(String v) { this.scopes = v; }
    public String getBoutonLibelle() { return boutonLibelle; }
    public void setBoutonLibelle(String v) { this.boutonLibelle = v; }
    public String getClaimCourriel() { return claimCourriel; }
    public void setClaimCourriel(String v) { this.claimCourriel = v; }
    public boolean isAutoProvision() { return autoProvision; }
    public void setAutoProvision(boolean autoProvision) { this.autoProvision = autoProvision; }
    public String getRoleParDefaut() { return roleParDefaut; }
    public void setRoleParDefaut(String v) { this.roleParDefaut = v; }

    /** Vrai si la configuration minimale (émetteur + identifiant client) est présente. */
    public boolean estConfigure() {
        return issuer != null && !issuer.isBlank()
                && clientId != null && !clientId.isBlank();
    }
}
