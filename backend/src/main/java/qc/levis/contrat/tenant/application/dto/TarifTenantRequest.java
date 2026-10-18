package qc.levis.contrat.tenant.application.dto;

/** Tarif d'abonnement d'un tenant (gestion superadmin). */
public record TarifTenantRequest(java.math.BigDecimal forfaitAnnuel, String devise,
                                     java.math.BigDecimal indexationPct, Integer nbUtilisateursInclus,
                                     String notes, Boolean actif) {}
