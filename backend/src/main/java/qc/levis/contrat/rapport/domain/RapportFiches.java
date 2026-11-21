package qc.levis.contrat.rapport.domain;

import java.util.List;
import java.util.Map;

/**
 * Rapport personnalisé sur les fiches d'un formulaire (CHU 6.03) : les questions retenues comme colonnes, un filtre par
 * statut, le décompte par statut et une ligne par fiche.
 */
public record RapportFiches(String titre, String formulaire, List<String> statutsRetenus, Map<String, Long> parStatut,
                            List<String> colonnes, List<List<String>> lignes) {

    public RapportFiches {
        statutsRetenus = List.copyOf(statutsRetenus);
        parStatut = Map.copyOf(parStatut);
        colonnes = List.copyOf(colonnes);
        lignes = lignes.stream().map(List::copyOf).toList();
    }

    public int total() {
        return lignes.size();
    }
}
