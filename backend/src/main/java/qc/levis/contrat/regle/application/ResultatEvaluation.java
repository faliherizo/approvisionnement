package qc.levis.contrat.regle.application;

import java.util.List;
import java.util.Map;

/**
 * Résultat de l'évaluation des règles contre un jeu de réponses :
 * champs masqués, champs rendus obligatoires, erreurs de validation, valeurs calculées, rôles de routage.
 */
public record ResultatEvaluation(List<String> champsMasques,
                                 List<String> champsObligatoires,
                                 List<String> erreurs,
                                 Map<String, java.math.BigDecimal> calculs,
                                 List<String> routage) {}
