package qc.levis.contrat.redaction.application.dto;

import java.util.List;
import java.util.Map;
import qc.levis.contrat.redaction.domain.Clause;
import qc.levis.contrat.redaction.domain.Document;
import qc.levis.contrat.redaction.domain.DocumentTemplate;

/** Résultat de recherche à facettes (contenus réutilisables + documents + templates), facettes portée et thématique. */
public record RechercheResultat(
            String terme,
            List<Clause> clauses,
            List<Document> documents,
            List<DocumentTemplate> templates,
            Map<String, Long> facettesCategorieClause,
            int total,
            Map<String, Long> facettesThematique) {}
