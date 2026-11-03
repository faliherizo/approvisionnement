package qc.levis.contrat.clm.domain;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/** Cycle de vie d'une trousse (UI-04) : seules les transitions déclarées ici sont permises. */
public enum StatutTrousse {
    BROUILLON, EN_TRAITEMENT, TERMINEE, REJETEE, ANNULEE;

    private static final Map<StatutTrousse, Set<StatutTrousse>> TRANSITIONS = Map.of(
            BROUILLON, EnumSet.of(EN_TRAITEMENT, ANNULEE),
            EN_TRAITEMENT, EnumSet.of(TERMINEE, REJETEE, ANNULEE, BROUILLON),
            REJETEE, EnumSet.of(BROUILLON, ANNULEE),
            TERMINEE, EnumSet.noneOf(StatutTrousse.class),
            ANNULEE, EnumSet.noneOf(StatutTrousse.class));

    public boolean peutPasserA(StatutTrousse cible) {
        return TRANSITIONS.get(this).contains(cible);
    }

    public StatutTrousse vers(StatutTrousse cible) {
        if (!peutPasserA(cible)) {
            throw new IllegalStateException("Transition interdite : " + this + " → " + cible);
        }
        return cible;
    }

    public boolean estModifiable() {
        return this == BROUILLON;
    }

    public boolean estFinal() {
        return this == TERMINEE || this == ANNULEE;
    }
}
