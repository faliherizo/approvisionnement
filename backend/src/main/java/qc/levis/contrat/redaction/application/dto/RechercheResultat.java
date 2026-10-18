package qc.levis.contrat.redaction.application.dto;

import java.util.List;

/** Résultat de recherche à facettes (contenus réutilisables + documents + templates). */
public record RechercheResultat(
            String terme,
            List<qc.levis.contrat.redaction.domain.Clause> clauses,
            List<qc.levis.contrat.redaction.domain.Document> documents,
            List<qc.levis.contrat.redaction.domain.DocumentTemplate> templates,
            java.util.Map<String, Long> facettesCategorieClause,
            int total) {}
