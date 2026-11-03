package qc.levis.contrat.formulaire.application.dto;

import jakarta.validation.constraints.NotBlank;

/** Création / mise à jour de l'en-tête d'un formulaire. {@code domaine} optionnel (RH, APPROVISIONNEMENT…). */
public record FormulaireRequest(@NotBlank String code, @NotBlank String nom, String domaine, Long parentId,
                                Integer conservationJours) {}
