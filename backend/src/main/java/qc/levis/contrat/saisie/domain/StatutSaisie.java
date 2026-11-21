package qc.levis.contrat.saisie.domain;

/**
 * Cycle de vie d'une demande (fiche), aligné sur l'état métier cible (architecture §15.1 :
 * DRAFT→SUBMITTED→IN_REVIEW→WAITING_FOR_REQUESTER→APPROVED/REJECTED→CLOSED, + CANCELLED).
 * Les transitions autorisées sont portées par l'énumération (invariant testable, refus par défaut).
 */
public enum StatutSaisie {
    BROUILLON,      // DRAFT
    SOUMIS,         // SUBMITTED
    EN_TRAITEMENT,  // IN_REVIEW
    A_COMPLETER,    // WAITING_FOR_REQUESTER
    VALIDE,         // APPROVED
    REJETE,         // REJECTED
    CLOTURE,        // CLOSED
    ANNULE;         // CANCELLED

    /** Vrai si la transition {@code this → cible} est permise. Toute autre transition est interdite. */
    public boolean peutAllerVers(StatutSaisie cible) {
        return switch (this) {
            case BROUILLON -> cible == SOUMIS || cible == ANNULE;
            case SOUMIS -> cible == EN_TRAITEMENT || cible == VALIDE || cible == REJETE;
            case EN_TRAITEMENT -> cible == A_COMPLETER || cible == VALIDE || cible == REJETE;
            case A_COMPLETER -> cible == SOUMIS;
            case VALIDE, REJETE -> cible == CLOTURE;
            case CLOTURE, ANNULE -> false;
        };
    }

    /**
     * Vrai si la fiche retient une place dans une activité à capacité limitée (CHU 3.06) : une fiche soumise et non
     * refusée ni annulée garde sa place ; un brouillon n'en réserve pas encore.
     */
    public boolean occupeUnePlace() {
        return this == SOUMIS || this == EN_TRAITEMENT || this == A_COMPLETER || this == VALIDE || this == CLOTURE;
    }

    /** Vrai si la demande est dans un état final (ne peut plus progresser côté traitement). */
    public boolean estTermine() {
        return this == CLOTURE || this == ANNULE;
    }
}
