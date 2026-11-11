package qc.levis.contrat.clm.domain;

import java.time.Duration;
import java.time.Instant;

/**
 * Rappels de non-réponse et échéances (ST-11), puis reprises des envois vers Salesforce (ST-12). Fonctions pures
 * de l'horloge : le planificateur leur fournit {@code maintenant}.
 */
public final class Relances {

    /** Reprises d'un retour de statut avant abandon, tant que l'organisation n'en fixe pas d'autres. */
    public static final int REPRISES_PAR_DEFAUT = 8;
    /** Bornes d'un rappel, par trousse comme par défaut de l'organisation. */
    public static final int FREQUENCE_MAX_JOURS = 30;
    public static final int RAPPELS_PLAFOND = 10;
    /** Rappels d'une nouvelle trousse, tant que l'organisation n'en fixe pas d'autres (UI-22). */
    public static final int FREQUENCE_PAR_DEFAUT_JOURS = 2;
    public static final int RAPPELS_PAR_DEFAUT = 3;
    /** Préavis d'une échéance imminente (F-07, alertes d'échéances), tant que l'organisation n'en fixe pas d'autre ; 0 = aucun. */
    public static final int PREAVIS_PAR_DEFAUT_JOURS = 1;
    public static final int PREAVIS_MAX_JOURS = 30;
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

    /** L'échéance approche : elle tombe dans le préavis et n'est pas encore dépassée. Un préavis nul n'alerte jamais. */
    public static boolean echeanceProche(Instant echeance, Instant maintenant, int preavisJours) {
        return echeance != null && preavisJours > 0 && !maintenant.isAfter(echeance)
                && !maintenant.isBefore(echeance.minus(Duration.ofDays(preavisJours)));
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

    public static boolean abandonner(int tentatives, int reprisesMax) {
        return tentatives >= reprisesMax;
    }
}
