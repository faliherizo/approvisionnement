package qc.levis.contrat.ia.application.dto;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.List;
import qc.levis.contrat.commun.domain.Enums;

/** Génération guidée d'un document (via template OU sélection manuelle), option de rattachement. */
public record AssistantGenerationRequest(
            @NotBlank String titre,
            Long templateId,
            Enums.TypeContrat type,
            Enums.ModeSollicitation modeSollicitation,
            BigDecimal valeurEstimee,
            String contexte,
            Long dossierId,
            List<Long> clauseIds) {}
