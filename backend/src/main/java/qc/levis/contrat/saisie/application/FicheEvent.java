package qc.levis.contrat.saisie.application;

import java.util.Map;

/**
 * Événement de domaine émis au changement d'état d'une fiche. Permet au module de notification (ou à
 * d'autres) de réagir SANS couplage direct entre contextes. {@code declencheur} : SOUMISSION/VALIDATION/REJET.
 */
public record FicheEvent(Long tenantId, Long formulaireId, Long saisieId, String declencheur, Map<String, Object> valeurs) {}
