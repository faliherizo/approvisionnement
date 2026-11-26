package qc.levis.contrat.audit.application;

import java.time.Instant;

/** Entrée du journal d'audit telle qu'un autre module la lit, sans dépendre de l'entité (ADR-006). */
public record EntreeAudit(Long id, Instant horodatage, String utilisateur, String action, String entite, Long entiteId, String details) {
}
