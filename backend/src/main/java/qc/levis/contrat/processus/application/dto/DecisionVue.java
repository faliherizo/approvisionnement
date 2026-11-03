package qc.levis.contrat.processus.application.dto;

import java.time.Instant;

/** Vue d'une décision (historique append-only d'une demande). */
public record DecisionVue(Long tacheId, String acteur, String decision, String commentaire, Instant decideLe) {}
