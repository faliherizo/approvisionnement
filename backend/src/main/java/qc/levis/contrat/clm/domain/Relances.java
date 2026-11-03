package qc.levis.contrat.clm.domain;

import java.time.Duration;
import java.time.Instant;

/**
 * Rappels de non-réponse et échéances (ST-11), puis reprises des envois vers Salesforce (ST-12). Fonctions pures
 * de l'horloge : le planificateur leur fournit {@code maintenant}.
 */
public final class Relances {

    public static final int REPRISES_MAX = 8;
    private static final Duration DELAI_INITIAL = Duration.ofMinutes(1);
    private static final Duration DELAI_PLAFOND = Duration.ofHours(1);

    private Relances() {
    }

    /** Un rappel est dû quand la fréquence est écoulée depuis l'activation ou le dernier rappel, dans la limite permise. */
    public static boolean rappelDu(Instant activeeLe, Instant dernierRappel, int rappelsEnvoyes, Instant maintenant,
                                   int frequenceJours, int rappelsMax) {
        if (activeeLe == null || frequenceJours < 1 || rappelsEnvoyes >= rappelsMax) {
            return false;
        }
        Instant reference = dernierRappel == null ? activeeLe : dernierRappel;
        return !maintenant.isBefore(reference.plus(Duration.ofDays(frequenceJours)));
    }

    public static boolean enRetard(Instant echeance, Instant maintenant) {
        return echeance != null && maintenant.isAfter(echeance);
    }

    public static Instant echeance(Instant activeeLe, Integer delaiJours) {
        return activeeLe == null || delaiJours == null ? null : activeeLe.plus(Duration.ofDays(delaiJours));
    }

    /** Recul exponentiel plafonné : 1, 2, 4… minutes, jamais plus d'une heure. */
    public static Instant prochainEssai(int tentatives, Instant maintenant) {
        long minutes = DELAI_INITIAL.toMinutes() << Math.min(Math.max(tentatives - 1, 0), 10);
        Duration delai = Duration.ofMinutes(Math.min(minutes, DELAI_PLAFOND.toMinutes()));
        return maintenant.plus(delai);
    }

    public static boolean abandonner(int tentatives) {
        return tentatives >= REPRISES_MAX;
    }
}
