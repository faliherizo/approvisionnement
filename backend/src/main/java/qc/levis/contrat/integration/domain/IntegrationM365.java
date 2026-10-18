package qc.levis.contrat.integration.domain;

import jakarta.persistence.*;

/**
 * Configuration d'intégration Microsoft 365 propre à un tenant (SharePoint, OneDrive, Teams,
 * Outlook). Saisie et gérée depuis l'espace d'administration du tenant (pas dans un fichier yml),
 * pour que chaque organisation branche son propre environnement. Tant qu'elle est absente ou
 * inactive, la publication reste simulée. Le secret client est stocké <strong>chiffré</strong>.
 */
@Entity
@Table(name = "integration_m365")
public class IntegrationM365 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", unique = true)
    private Long tenantId;

    /** Active les dépôts réels via Microsoft Graph pour ce tenant (sinon simulation). */
    @Column(nullable = false)
    private boolean actif = false;

    @Column(name = "ms_tenant_id") private String msTenantId;
    @Column(name = "client_id") private String clientId;

    /** Secret client chiffré (AES). Jamais exposé en clair via l'API. */
    @Column(name = "client_secret_chiffre", length = 1000) private String clientSecretChiffre;

    @Column(name = "sharepoint_site_id") private String sharepointSiteId;
    @Column(name = "drive_id") private String driveId;
    @Column(name = "teams_team_id") private String teamsTeamId;
    @Column(name = "teams_channel_id") private String teamsChannelId;
    @Column(name = "outlook_expediteur") private String outlookExpediteur;
    @Column(name = "dossier_racine") private String dossierRacine;

    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }
    public String getMsTenantId() { return msTenantId; }
    public void setMsTenantId(String v) { this.msTenantId = v; }
    public String getClientId() { return clientId; }
    public void setClientId(String v) { this.clientId = v; }
    public String getClientSecretChiffre() { return clientSecretChiffre; }
    public void setClientSecretChiffre(String v) { this.clientSecretChiffre = v; }
    public String getSharepointSiteId() { return sharepointSiteId; }
    public void setSharepointSiteId(String v) { this.sharepointSiteId = v; }
    public String getDriveId() { return driveId; }
    public void setDriveId(String v) { this.driveId = v; }
    public String getTeamsTeamId() { return teamsTeamId; }
    public void setTeamsTeamId(String v) { this.teamsTeamId = v; }
    public String getTeamsChannelId() { return teamsChannelId; }
    public void setTeamsChannelId(String v) { this.teamsChannelId = v; }
    public String getOutlookExpediteur() { return outlookExpediteur; }
    public void setOutlookExpediteur(String v) { this.outlookExpediteur = v; }
    public String getDossierRacine() { return dossierRacine; }
    public void setDossierRacine(String v) { this.dossierRacine = v; }

    /** Vrai si la configuration minimale d'authentification est présente. */
    public boolean estConfigure() {
        return msTenantId != null && !msTenantId.isBlank()
                && clientId != null && !clientId.isBlank()
                && clientSecretChiffre != null && !clientSecretChiffre.isBlank();
    }
}
