package qc.levis.contrat.regle.domain;

/**
 * Condition élémentaire d'une règle : « le champ {@code champ} {@code operateur} {@code valeur} ».
 * {@code operateur} reste une chaîne (config-driven, non figée) : EGAL, DIFFERENT, SUP, INF, SUPEGAL,
 * INFEGAL, CONTIENT, VIDE, NON_VIDE.
 */
public record ConditionDef(String champ, String operateur, String valeur) {}
