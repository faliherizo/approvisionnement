package qc.levis.contrat.clm.application;

import java.util.Optional;
import qc.levis.contrat.clm.domain.Trousse;

/**
 * Condition supplémentaire au lancement d'une trousse, apportée par une fonction optionnelle du module (ex. une négociation
 * en cours, RST-55) : le lancement s'étend par ajout d'une condition, sans modifier ses règles propres.
 */
public interface ConditionLancement {

    /** Motif qui empêche de lancer la trousse, s'il y en a un. */
    Optional<String> empechement(Trousse t);
}
