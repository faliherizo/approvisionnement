package qc.levis.contrat.reference.application.dto;

import jakarta.validation.constraints.NotBlank;

/** Création/mise à jour d'une valeur de référence ; {@code detail} n'est retenu que si la liste en prévoit un. */
public record ReferenceRequest(
            @NotBlank String categorie,
            @NotBlank String code,
            @NotBlank String libelle,
            String detail,
            Integer ordre,
            Boolean actif) {}
