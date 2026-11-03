package qc.levis.contrat.paie.application.dto;

/**
 * Demande d'aperçu de paie : salaire brut de la période, fréquence (weekly/biweekly/semimonthly/monthly),
 * et {@code sansRetenues} (travailleur indépendant / hors Canada : net = brut).
 */
public record ApercuPaieRequest(double grossSalary, String frequency, boolean sansRetenues) {}
