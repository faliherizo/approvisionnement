package qc.levis.contrat.reference.application.dto;

import jakarta.validation.constraints.NotBlank;

/** Création/mise à jour d'une valeur de référence. */
public record ReferenceRequest(
            @NotBlank String categorie,
            @NotBlank String code,
            @NotBlank String libelle,
            Integer ordre,
            Boolean actif) {}
