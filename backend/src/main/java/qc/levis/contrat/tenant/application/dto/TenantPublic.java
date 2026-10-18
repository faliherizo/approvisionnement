package qc.levis.contrat.tenant.application.dto;

/** Vue publique d'un tenant (page de connexion) incluant le thème. */
public record TenantPublic(String slug, String titre, String sousTitre, Theme theme) {}
