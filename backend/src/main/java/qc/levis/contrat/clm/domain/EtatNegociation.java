package qc.levis.contrat.clm.domain;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Cycle d'une négociation avec un tiers (RST-55) : la main passe de la contrepartie à l'organisation et revient, jusqu'à
 * l'entente, que seule l'organisation clôt. Seules les transitions déclarées ici sont permises.
 */
public enum EtatNegociation {
    ATTENTE_CONTREPARTIE, ATTENTE_INTERNE, ENTENTE, CLOSE, ANNULEE;

    private static final Map<EtatNegociation, Set<EtatNegociation>> TRANSITIONS = Map.of(
            ATTENTE_CONTREPARTIE, EnumSet.of(ATTENTE_INTERNE, ENTENTE, ANNULEE),
            ATTENTE_INTERNE, EnumSet.of(ATTENTE_CONTREPARTIE, ENTENTE, ANNULEE),
            ENTENTE, EnumSet.of(CLOSE, ANNULEE),
            CLOSE, EnumSet.noneOf(EtatNegociation.class),
            ANNULEE, EnumSet.noneOf(EtatNegociation.class));

    public EtatNegociation vers(EtatNegociation cible) {
        if (!TRANSITIONS.get(this).contains(cible)) {
            throw new TransitionInterdite(this, cible);
        }
        return cible;
    }

    /** Une négociation close ou annulée ne s'échange plus : son lien est sans effet. */
    public boolean estFinal() {
        return TRANSITIONS.get(this).isEmpty();
    }

    /** Refus typé : l'action demandée n'est pas permise dans l'état courant. */
    public static final class TransitionInterdite extends IllegalStateException {

        private final EtatNegociation depuis;
        private final EtatNegociation vers;

        TransitionInterdite(EtatNegociation depuis, EtatNegociation vers) {
            super("Action impossible : la négociation est à l'état " + depuis + ", elle ne peut passer à " + vers + ".");
            this.depuis = depuis;
            this.vers = vers;
        }

        public EtatNegociation depuis() {
            return depuis;
        }

        public EtatNegociation vers() {
            return vers;
        }
    }
}
