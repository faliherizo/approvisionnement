package qc.levis.contrat.auth.domain;

import java.time.Duration;
import java.time.Instant;

/**
 * Limites d'une session (ST-01) : elle prend fin après un délai d'inactivité, et au plus tard après une durée maximale
 * depuis la connexion, quelle que soit l'activité. Les bornes protègent contre un réglage dangereux (session sans fin)
 * comme contre un réglage inutilisable.
 */
public record LimitesSession(Duration inactivite, Duration dureeMax) {

    public static final Duration INACTIVITE_MIN = Duration.ofMinutes(5);
    public static final Duration INACTIVITE_MAX = Duration.ofHours(4);
    public static final Duration DUREE_MAX_MIN = Duration.ofMinutes(15);
    public static final Duration DUREE_MAX_MAX = Duration.ofHours(24);

    /** Verdict sur une session : valide, terminée par inactivité, ou terminée par sa durée maximale. */
    public enum Verdict { VALIDE, INACTIVE, EXPIREE }

    public LimitesSession {
        if (inactivite == null || dureeMax == null) {
            throw new IllegalArgumentException("Délai d'inactivité et durée maximale requis.");
        }
        if (inactivite.compareTo(INACTIVITE_MIN) < 0 || inactivite.compareTo(INACTIVITE_MAX) > 0) {
            throw new IllegalArgumentException("Délai d'inactivité : de 5 minutes à 4 heures.");
        }
        if (dureeMax.compareTo(DUREE_MAX_MIN) < 0 || dureeMax.compareTo(DUREE_MAX_MAX) > 0) {
            throw new IllegalArgumentException("Durée maximale de session : de 15 minutes à 24 heures.");
        }
        if (inactivite.compareTo(dureeMax) > 0) {
            throw new IllegalArgumentException("Le délai d'inactivité ne peut pas dépasser la durée maximale de la session.");
        }
    }

    /**
     * @param debut          connexion (début de session)
     * @param derniereActivite émission du jeton présenté : chaque activité en réémet un
     */
    public Verdict verifier(Instant debut, Instant derniereActivite, Instant maintenant) {
        if (!maintenant.isBefore(debut.plus(dureeMax))) {
            return Verdict.EXPIREE;
        }
        return maintenant.isBefore(derniereActivite.plus(inactivite)) ? Verdict.VALIDE : Verdict.INACTIVE;
    }

    /** Validité d'un jeton réémis maintenant : jusqu'à la fin de l'inactivité tolérée, jamais au-delà de la durée maximale. */
    public Duration validiteRenouvelee(Instant debut, Instant maintenant) {
        Duration restante = Duration.between(maintenant, debut.plus(dureeMax));
        return restante.compareTo(inactivite) < 0 ? restante : inactivite;
    }
}
