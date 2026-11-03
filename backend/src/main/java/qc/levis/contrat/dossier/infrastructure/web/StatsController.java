package qc.levis.contrat.dossier.infrastructure.web;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.commun.module.infrastructure.web.ModuleRequis;
import qc.levis.contrat.redaction.application.repository.ClauseRepository;
import qc.levis.contrat.redaction.application.repository.DocumentRepository;
import qc.levis.contrat.redaction.domain.Document;

/** Tableau de bord analytique (écart CLM E-3 : reporting). */
@RestController
@ModuleRequis("redaction")
@RequestMapping("/api/stats")
public class StatsController {

    private final DocumentRepository documents;
    private final ClauseRepository clauses;

    public StatsController(DocumentRepository documents, ClauseRepository clauses) {
        this.documents = documents;
        this.clauses = clauses;
    }

    @GetMapping
    public Map<String, Object> stats() {
        var docs = documents.findByTenantId(TenantContext.get());
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("nbDocuments", docs.size());
        res.put("nbClauses", clauses.findByActifTrueAndTenantId(TenantContext.get()).size());
        res.put("parStatut", compte(docs, d -> d.getStatut().name()));
        res.put("parType", compte(docs, d -> d.getType().name()));
        res.put("parMode", compte(docs, d ->
                d.getModeSollicitation() != null ? d.getModeSollicitation().name() : "NON_DEFINI"));
        return res;
    }

    private Map<String, Long> compte(java.util.List<Document> docs, Function<Document, String> cle) {
        return docs.stream().collect(Collectors.groupingBy(
                cle, LinkedHashMap::new, Collectors.counting()));
    }
}
