package qc.levis.contrat.integration.domain;

import jakarta.persistence.*;

/**
 * Configuration d'authentification SAML 2.0 (Service Provider) propre à un tenant. Saisie et gérée
 * depuis l'espace d'administration du tenant, pour que chaque organisation branche son propre
 * fournisseur d'identité (ADFS, Azure AD, Shibboleth, tout IdP SAML). Tant qu'elle est absente ou
 * inactive, la page de connexion n'affiche que l'authentification locale. Le certificat de signature
 * de l'IdP est <strong>public</strong> (aucun secret n'est stocké côté SP) : il est conservé en clair.
 */
@Entity
@Table(name = "integration_saml")
public class IntegrationSaml {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", unique = true)
    private Long tenantId;

    /** Active la connexion SAML pour ce tenant (sinon la page de connexion l'ignore). */
    @Column(nullable = false)
    private boolean actif = false;

    /** Identifiant d'entité (EntityID) de l'IdP. */
    @Column(name = "idp_entity_id", length = 500) private String idpEntityId;

    /** URL du service Single Sign-On de l'IdP (redirect binding). */
    @Column(name = "idp_sso_url", length = 500) private String idpSsoUrl;

    /** Certificat X.509 de signature de l'IdP (PEM). Public : conservé en clair. */
    @Column(name = "idp_certificat", length = 4000) private String idpCertificat;

    /** Identifiant d'entité (EntityID) de ce Service Provider. */
    @Column(name = "sp_entity_id", length = 500) private String spEntityId;

    /** Nom de l'attribut portant le courriel dans l'assertion (défaut « email » ; sinon NameID). */
    @Column(name = "claim_courriel", length = 100) private String claimCourriel = "email";

    /** Libellé du bouton affiché sur la page de connexion. */
    @Column(name = "bouton_libelle", length = 200) private String boutonLibelle = "Connexion SAML";

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
    public String getIdpEntityId() { return idpEntityId; }
    public void setIdpEntityId(String v) { this.idpEntityId = v; }
    public String getIdpSsoUrl() { return idpSsoUrl; }
    public void setIdpSsoUrl(String v) { this.idpSsoUrl = v; }
    public String getIdpCertificat() { return idpCertificat; }
    public void setIdpCertificat(String v) { this.idpCertificat = v; }
    public String getSpEntityId() { return spEntityId; }
    public void setSpEntityId(String v) { this.spEntityId = v; }
    public String getClaimCourriel() { return claimCourriel; }
    public void setClaimCourriel(String v) { this.claimCourriel = v; }
    public String getBoutonLibelle() { return boutonLibelle; }
    public void setBoutonLibelle(String v) { this.boutonLibelle = v; }
    public boolean isAutoProvision() { return autoProvision; }
    public void setAutoProvision(boolean autoProvision) { this.autoProvision = autoProvision; }
    public String getRoleParDefaut() { return roleParDefaut; }
    public void setRoleParDefaut(String v) { this.roleParDefaut = v; }

    /** Vrai si la configuration minimale (EntityID, SSO URL et certificat de l'IdP) est présente. */
    public boolean estConfigure() {
        return idpEntityId != null && !idpEntityId.isBlank()
                && idpSsoUrl != null && !idpSsoUrl.isBlank()
                && idpCertificat != null && !idpCertificat.isBlank();
    }
}
