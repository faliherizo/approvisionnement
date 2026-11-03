package qc.levis.contrat.securite.application;

import java.util.List;
import qc.levis.contrat.securite.domain.RegleAcces;

/**
 * Décision PURE d'autorisation granulaire, en REFUS PAR DÉFAUT : l'action est permise seulement si au
 * moins une règle correspond (sujet de l'utilisateur, portée, cible, action), « * » faisant joker.
 */
public final class MoteurAcces {

    private MoteurAcces() {}

    public static boolean autorise(List<RegleAcces> regles, SujetsUtilisateur u,
                                   String portee, String cible, String action) {
        if (regles == null) return false;
        for (RegleAcces r : regles) {
            if (sujetCorrespond(r, u) && champ(r.portee(), portee) && champ(r.cible(), cible) && champ(r.action(), action))
                return true;
        }
        return false;
    }

    private static boolean sujetCorrespond(RegleAcces r, SujetsUtilisateur u) {
        return switch (r.sujetType() == null ? "" : r.sujetType().toUpperCase()) {
            case "ROLE" -> u.roles() != null && u.roles().contains(r.sujet());
            case "GROUPE" -> u.groupes() != null && u.groupes().contains(r.sujet());
            case "UTILISATEUR" -> r.sujet() != null && r.sujet().equals(u.utilisateur());
            default -> false;
        };
    }

    private static boolean champ(String regle, String demande) {
        return "*".equals(regle) || (regle != null && regle.equals(demande));
    }
}
