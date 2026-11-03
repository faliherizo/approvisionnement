package qc.levis.contrat.formulaire.domain;

import java.util.Set;

/**
 * Cycle de vie d'une version de formulaire. Transitions autorisées encodées ici (logique pure) :
 * BROUILLON → EN_APPROBATION → PUBLIE → ARCHIVE, avec retours possibles vers BROUILLON.
 */
public enum StatutFormulaire {
    BROUILLON,
    EN_APPROBATION,
    PUBLIE,
    ARCHIVE;

    private static final java.util.Map<StatutFormulaire, Set<StatutFormulaire>> TRANSITIONS = java.util.Map.of(
            BROUILLON, Set.of(EN_APPROBATION, ARCHIVE),
            EN_APPROBATION, Set.of(PUBLIE, BROUILLON),
            PUBLIE, Set.of(ARCHIVE, BROUILLON),
            ARCHIVE, Set.of()
    );

    public boolean peutAllerVers(StatutFormulaire cible) {
        return TRANSITIONS.getOrDefault(this, Set.of()).contains(cible);
    }
}
