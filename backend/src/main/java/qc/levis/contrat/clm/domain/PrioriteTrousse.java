package qc.levis.contrat.clm.domain;

import java.time.Duration;
import java.util.Locale;

/**
 * Priorité d'un dossier (Annexe 10 IQ, AF-46) : elle ordonne le suivi et « Mes tâches » et rapproche les rappels. Choisie
 * par l'initiateur, ou à défaut déduite du niveau de risque de la trousse.
 */
public enum PrioriteTrousse {
    BASSE(1), NORMALE(1), HAUTE(2), CRITIQUE(4);

    /** Plancher d'un intervalle de rappel accéléré : jamais plus de quatre rappels par jour. */
    static final Duration INTERVALLE_MIN = Duration.ofHours(6);

    /** Diviseur de l'intervalle de rappel de l'organisation : 2 pour une trousse haute, 4 pour une critique. */
    private final int acceleration;

    PrioriteTrousse(int acceleration) {
        this.acceleration = acceleration;
    }

    /** Règle de dérivation : un risque faible donne une priorité basse, moyen normale, élevé haute ; critique se choisit. */
    public static PrioriteTrousse deduite(Identite.Risque risque) {
        return switch (risque) {
            case FAIBLE -> BASSE;
            case MOYEN -> NORMALE;
            case ELEVE -> HAUTE;
        };
    }

    /** Code reçu de l'API ou de l'écran, sans égard à la casse ni aux espaces. */
    public static PrioriteTrousse depuis(String code) {
        String c = code == null ? "" : code.strip().toUpperCase(Locale.ROOT);
        for (PrioriteTrousse p : values()) {
            if (p.name().equals(c)) {
                return p;
            }
        }
        throw new PrioriteInconnueException(code);
    }

    /** Rang de tri : plus il est grand, plus le dossier passe tôt. */
    public int rang() {
        return ordinal();
    }

    /** Intervalle entre deux rappels : celui de l'organisation, divisé selon la priorité, jamais sous le plancher. */
    public Duration intervalleRappel(int frequenceJours) {
        Duration reduit = Duration.ofDays(frequenceJours).dividedBy(acceleration);
        return reduit.compareTo(INTERVALLE_MIN) < 0 ? INTERVALLE_MIN : reduit;
    }

    /** Priorité hors de la liste BASSE, NORMALE, HAUTE, CRITIQUE. */
    public static final class PrioriteInconnueException extends IllegalArgumentException {
        public PrioriteInconnueException(String code) {
            super("Priorité inconnue : " + code + " (BASSE, NORMALE, HAUTE ou CRITIQUE).");
        }
    }
}
