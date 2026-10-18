package qc.levis.contrat.integration.application.dto;

/** Vue de la configuration M365 renvoyée au frontend : SANS le secret (juste sa présence). */
public record IntegrationM365Vue(boolean actif, String msTenantId, String clientId,
                                     String sharepointSiteId, String driveId, String teamsTeamId,
                                     String teamsChannelId, String outlookExpediteur,
                                     String dossierRacine, boolean secretDefini) {}
