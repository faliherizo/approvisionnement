package qc.levis.contrat.redaction.application;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import qc.levis.contrat.commun.domain.Enums;
import qc.levis.contrat.redaction.application.dto.RechercheResultat;
import qc.levis.contrat.redaction.application.repository.ClauseRepository;
import qc.levis.contrat.redaction.application.repository.DocumentRepository;
import qc.levis.contrat.redaction.application.repository.DocumentTemplateRepository;
import qc.levis.contrat.redaction.domain.Clause;
import qc.levis.contrat.redaction.domain.CriteresRechercheClause;
import qc.levis.contrat.redaction.domain.Document;
import qc.levis.contrat.redaction.domain.DocumentTemplate;

/**
 * Recherche multicritère sur le dépôt central (art. 7.4 ; Lévis EF1, LEV-C-02) : les clauses par terme, portée,
 * thématique, type de contrat et caractère normatif ; les documents et modèles par terme et type. Les facettes
 * (portée, thématique) comptent les clauses retenues, pour affiner la recherche suivante.
 */
@Service
public class RechercheMulticritere {

    private final ClauseRepository clauses;
    private final DocumentRepository documents;
    private final DocumentTemplateRepository templates;

    public RechercheMulticritere(ClauseRepository clauses, DocumentRepository documents, DocumentTemplateRepository templates) {
        this.clauses = clauses;
        this.documents = documents;
        this.templates = templates;
    }

    public RechercheResultat rechercher(Long tenant, String q, CriteresRechercheClause criteres) {
        String terme = criteres.terme();
        Enums.TypeContrat type = criteres.typeContrat();
        List<Clause> clausesTrouvees = clauses.findByActifTrueAndTenantId(tenant).stream().filter(criteres::retient).toList();
        List<Document> docsTrouves = documents.findByTenantIdAndActifTrue(tenant).stream()
                .filter(d -> contient(terme, d.getTitre(), d.getContexte()))
                .filter(d -> type == null || d.getType() == type)
                .toList();
        List<DocumentTemplate> templatesTrouves = templates.findByTenantId(tenant).stream()
                .filter(t -> contient(terme, t.getCode(), t.getNom(), t.getDescription()))
                .filter(t -> type == null || t.getType() == type)
                .toList();
        int total = clausesTrouvees.size() + docsTrouves.size() + templatesTrouves.size();
        return new RechercheResultat(q, clausesTrouvees, docsTrouves, templatesTrouves,
                facettes(clausesTrouvees, c -> c.getCategorie().name()), total,
                facettes(clausesTrouvees, Clause::getSection));
    }

    /** Thématiques (sections) des clauses actives de l'organisation, triées, pour alimenter le filtre. */
    public List<String> thematiques(Long tenant) {
        return List.copyOf(clauses.findByActifTrueAndTenantId(tenant).stream()
                .map(Clause::getSection).filter(Objects::nonNull).map(String::strip).filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(TreeSet::new)));
    }

    private static Map<String, Long> facettes(List<Clause> trouvees, Function<Clause, String> cle) {
        Map<String, Long> facettes = new LinkedHashMap<>();
        trouvees.stream().map(cle).filter(Objects::nonNull).map(String::strip).filter(k -> !k.isEmpty()).forEach(k -> facettes.merge(k, 1L, Long::sum));
        return facettes;
    }

    private static boolean contient(String terme, String... champs) {
        if (terme.isEmpty()) {
            return true;
        }
        for (String champ : champs) {
            if (champ != null && champ.toLowerCase(Locale.FRENCH).contains(terme)) {
                return true;
            }
        }
        return false;
    }
}
