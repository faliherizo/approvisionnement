package qc.levis.contrat.reference.application.dto;

import java.util.List;

/**
 * Liste de référence qu'un module déclare au démarrage, avec ses valeurs de plateforme par défaut. Les valeurs ne sont
 * amorcées qu'une fois : celles que la plateforme a modifiées depuis ne sont jamais écrasées.
 */
public record DeclarationCategorie(String code, String module, String libelle, String aide, String libelleDetail, int ordre,
                                   List<Valeur> valeurs) {

    public record Valeur(String code, String libelle, String detail) {
    }

    public DeclarationCategorie {
        valeurs = valeurs == null ? List.of() : List.copyOf(valeurs);
    }
}
