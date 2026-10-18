package qc.levis.contrat.ia.application.dto;

import java.util.List;
import qc.levis.contrat.commun.domain.Enums;

/** Recommandations de l'assistant : mode suggéré, templates pertinents, clauses éligibles. */
public record AssistantSuggestion(
            Enums.ModeSollicitation modeSuggere,
            List<TemplateRecommande> templatesRecommandes,
            List<ClauseApercu> clausesEligibles) {}
