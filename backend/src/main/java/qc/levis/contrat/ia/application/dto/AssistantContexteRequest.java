package qc.levis.contrat.ia.application.dto;

import java.math.BigDecimal;
import qc.levis.contrat.commun.domain.Enums;

/** Contexte saisi dans le parcours guidé, pour obtenir des recommandations. */
public record AssistantContexteRequest(
            Enums.TypeContrat type,
            Enums.TypeAcquisition categorieAcquisition,
            BigDecimal valeurEstimee,
            String contexte) {}
