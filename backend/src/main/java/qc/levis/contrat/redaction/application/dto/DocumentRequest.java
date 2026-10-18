package qc.levis.contrat.redaction.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import qc.levis.contrat.commun.domain.Enums;

public record DocumentRequest(
            @NotBlank String titre,
            @NotNull Enums.TypeContrat type,
            Enums.ModeSollicitation modeSollicitation,
            BigDecimal valeurEstimee,
            String contexte,
            String numeroAO,
            String organismePublic,
            String prestataire,
            String gabaritCode,
            Long dossierId) {}
