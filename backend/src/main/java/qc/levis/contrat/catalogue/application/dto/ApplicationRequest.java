package qc.levis.contrat.catalogue.application.dto;

import jakarta.validation.constraints.NotBlank;

/** Création / mise à jour d'une application (espace fonctionnel). */
public record ApplicationRequest(@NotBlank String code, @NotBlank String nom, String domaine,
                                 String environnement, String statut, String proprietaire, String description) {}
