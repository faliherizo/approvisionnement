package qc.levis.contrat.notification.application.dto;

import jakarta.validation.constraints.NotBlank;

/** Création/mise à jour d'un gabarit de notification. */
public record ModeleRequest(@NotBlank String code, @NotBlank String canal, String sujet, String corps) {}
