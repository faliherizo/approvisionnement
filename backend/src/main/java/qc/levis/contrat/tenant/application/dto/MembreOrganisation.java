package qc.levis.contrat.tenant.application.dto;

/** Personne active d'une organisation, telle que d'autres modules peuvent la désigner (sans ses rôles ni secrets). */
public record MembreOrganisation(String courriel, String nom) {}
