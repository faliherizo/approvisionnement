package qc.levis.contrat.saisie.domain;

import java.util.List;
import java.util.Map;

/**
 * Champ à choix d'un formulaire (liste, boutons radio, cases) avec ses choix et, s'il y a lieu, le nombre de places de
 * chaque choix (CHU 3.06). Un choix sans capacité déclarée est illimité.
 */
public record ChampAChoix(String cle, String libelle, List<String> choix, Map<String, Integer> capacites) {

    public ChampAChoix {
        choix = List.copyOf(choix);
        capacites = Map.copyOf(capacites);
        capacites.forEach((c, n) -> {
            if (n < 0) throw new IllegalArgumentException("Capacité négative pour le choix « " + c + " » du champ " + cle);
        });
    }

    public boolean estLimite() {
        return !capacites.isEmpty();
    }
}
