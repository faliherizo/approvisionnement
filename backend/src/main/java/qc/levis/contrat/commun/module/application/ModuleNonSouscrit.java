package qc.levis.contrat.commun.module.application;

/** Fonctionnalité d'un module que l'organisation courante n'a pas souscrit : refus 403, quel que soit le rôle. */
public class ModuleNonSouscrit extends RuntimeException {

    public ModuleNonSouscrit(String codes) {
        super(message(codes));
    }

    /** Libellé unique du refus, que la garde des contrôleurs et les services emploient à l'identique. */
    public static String message(String codes) {
        return "Fonctionnalité non souscrite par votre organisation (module « " + codes + " »).";
    }
}
