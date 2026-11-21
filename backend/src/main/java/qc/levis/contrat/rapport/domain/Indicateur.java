package qc.levis.contrat.rapport.domain;

import java.util.List;

/**
 * Indicateur d'un tableau de bord (CHU 6.04) : ce qu'il compte (les fiches d'un formulaire, filtrées par statut) et comment
 * il les regroupe. Un compteur ne regroupe pas ; un histogramme regroupe par statut ou par réponse à une question ; une
 * évolution regroupe par mois de soumission.
 */
public record Indicateur(String titre, Type type, Long formulaireId, Regroupement regroupement, String champ, List<String> statuts) {

    public enum Type { COMPTEUR, BARRES, EVOLUTION }

    public enum Regroupement { AUCUN, STATUT, CHAMP, MOIS }

    public Indicateur {
        if (titre == null || titre.isBlank() || titre.length() > 100) throw new IllegalArgumentException("Titre d'indicateur requis (100 caractères au plus).");
        if (type == null || formulaireId == null) throw new IllegalArgumentException("Type et formulaire de l'indicateur requis.");
        regroupement = regroupement == null ? Regroupement.AUCUN : regroupement;
        statuts = statuts == null ? List.of() : List.copyOf(statuts);
        exigerCoherence(type, regroupement, champ);
    }

    private static void exigerCoherence(Type type, Regroupement r, String champ) {
        boolean ok = switch (type) {
            case COMPTEUR -> r == Regroupement.AUCUN;
            case BARRES -> r == Regroupement.STATUT || (r == Regroupement.CHAMP && champ != null && !champ.isBlank());
            case EVOLUTION -> r == Regroupement.MOIS;
        };
        if (!ok) throw new IllegalArgumentException("Regroupement « " + r + " » incompatible avec un indicateur « " + type + " ».");
    }
}
