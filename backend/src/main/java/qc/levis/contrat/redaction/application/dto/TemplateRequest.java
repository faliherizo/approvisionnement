package qc.levis.contrat.redaction.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import qc.levis.contrat.commun.domain.Enums;

public record TemplateRequest(
            @NotBlank String code,
            @NotBlank String nom,
            @NotNull Enums.TypeContrat type,
            Enums.ModeSollicitation modeSollicitation,
            Enums.TypeAcquisition categorieAcquisition,
            String description,
            String titreParDefaut,
            String contexte,
            String gabaritCode,
            List<String> clauseCodes,
            List<String> annexes) {}
