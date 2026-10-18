package qc.levis.contrat.redaction.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import qc.levis.contrat.commun.domain.Enums;

/** Condition d'éligibilité définie par l'admin (moteur de règles). */
public record ConditionRequest(
            @NotNull Enums.AttributCondition attribut,
            @NotBlank String valeur) {}
