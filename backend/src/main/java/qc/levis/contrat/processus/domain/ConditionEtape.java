package qc.levis.contrat.processus.domain;

/**
 * Condition de routage d'une étape de workflow : « le champ {@code champ} {@code operateur} {@code valeur} »
 * (ex. montant SUP 10000). {@code operateur} reste une chaîne config-driven : EGAL, DIFFERENT, SUP, INF,
 * SUPEGAL, INFEGAL, CONTIENT, VIDE, NON_VIDE. Value object du contexte processus (indépendant de regle).
 */
public record ConditionEtape(String champ, String operateur, String valeur) {}
