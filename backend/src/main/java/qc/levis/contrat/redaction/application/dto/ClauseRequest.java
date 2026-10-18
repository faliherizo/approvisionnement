package qc.levis.contrat.redaction.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import qc.levis.contrat.commun.domain.Enums;

public record ClauseRequest(
            @NotBlank String code,
            @NotBlank String titre,
            @NotNull Enums.ClauseCategorie categorie,
            @NotBlank String corps,
            String numero,
            String section,
            boolean normative,
            List<String> dependances,
            List<String> exclusions,
            List<ConditionRequest> conditions) {}
