package qc.levis.contrat.redaction.infrastructure.web;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import qc.levis.contrat.commun.domain.Enums;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.redaction.application.dto.RechercheResultat;
import qc.levis.contrat.redaction.application.repository.ClauseRepository;
import qc.levis.contrat.redaction.application.repository.DocumentRepository;
import qc.levis.contrat.redaction.application.repository.DocumentTemplateRepository;
import qc.levis.contrat.redaction.domain.Clause;
import qc.levis.contrat.redaction.domain.Document;
import qc.levis.contrat.redaction.domain.DocumentTemplate;

/**
 * Recherche enrichie sur le dépôt central (art. 7.4, écart 10) : contenus réutilisables
 * (clauses), documents et templates, avec facettes (catégorie de clause, type de document).
 */
@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final ClauseRepository clauses;
    private final DocumentRepository documents;
    private final DocumentTemplateRepository templates;

    public SearchController(ClauseRepository clauses, DocumentRepository documents,
                            DocumentTemplateRepository templates) {
        this.clauses = clauses;
        this.documents = documents;
        this.templates = templates;
    }

    @GetMapping
    public RechercheResultat rechercher(
            @RequestParam("q") String q,
            @RequestParam(value = "categorie", required = false) Enums.ClauseCategorie categorie,
            @RequestParam(value = "type", required = false) Enums.TypeContrat type) {

        String terme = q == null ? "" : q.toLowerCase(Locale.FRENCH).trim();
        Long tenant = TenantContext.get();

        List<Clause> clausesTrouvees = clauses.findByActifTrueAndTenantId(tenant).stream()
                .filter(c -> contient(c.getCode(), terme) || contient(c.getTitre(), terme)
                        || contient(c.getCorps(), terme))
                .filter(c -> categorie == null || c.getCategorie() == categorie)
                .toList();

        List<Document> docsTrouves = documents.findByTenantIdAndActifTrue(tenant).stream()
                .filter(d -> contient(d.getTitre(), terme) || contient(d.getContexte(), terme))
                .filter(d -> type == null || d.getType() == type)
                .toList();

        List<DocumentTemplate> templatesTrouves = templates.findByTenantId(tenant).stream()
                .filter(t -> contient(t.getCode(), terme) || contient(t.getNom(), terme)
                        || contient(t.getDescription(), terme))
                .filter(t -> type == null || t.getType() == type)
                .toList();

        Map<String, Long> facettes = new LinkedHashMap<>();
        for (Clause c : clausesTrouvees) {
            facettes.merge(c.getCategorie().name(), 1L, Long::sum);
        }

        int total = clausesTrouvees.size() + docsTrouves.size() + templatesTrouves.size();
        return new RechercheResultat(q, clausesTrouvees, docsTrouves, templatesTrouves, facettes, total);
    }

    private boolean contient(String champ, String terme) {
        return terme.isEmpty() || (champ != null && champ.toLowerCase(Locale.FRENCH).contains(terme));
    }
}
