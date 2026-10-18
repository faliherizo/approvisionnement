package qc.levis.contrat.integration.application.dto;

/** Saisie de la configuration Microsoft 365 d'un tenant (espace admin). Secret write-only. */
public record IntegrationM365Request(Boolean actif, String msTenantId, String clientId,
                                         String clientSecret, String sharepointSiteId, String driveId,
                                         String teamsTeamId, String teamsChannelId,
                                         String outlookExpediteur, String dossierRacine) {}
