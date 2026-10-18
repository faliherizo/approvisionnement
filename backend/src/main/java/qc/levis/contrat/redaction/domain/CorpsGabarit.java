package qc.levis.contrat.redaction.domain;

/**
 * Mode de rendu du corps d'un document : structure contractuelle numérotée ou
 * grille de prix (bordereau). Le moteur de rendu choisit sa stratégie à partir de
 * cette valeur ; un gabarit dynamique se contente d'en sélectionner une (on
 * n'écrit pas de logique de rendu depuis l'interface).
 */
public enum CorpsGabarit {
    CONTRAT, BORDEREAU
}
