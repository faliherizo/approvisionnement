package qc.levis.contrat.redaction.infrastructure.web;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import qc.levis.contrat.commun.domain.Enums;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.commun.module.infrastructure.web.ModuleRequis;
import qc.levis.contrat.redaction.application.RechercheMulticritere;
import qc.levis.contrat.redaction.application.dto.RechercheResultat;
import qc.levis.contrat.redaction.domain.CriteresRechercheClause;

/**
 * Recherche enrichie sur le dépôt central (art. 7.4, écart 10 ; Lévis LEV-C-02) : contenus réutilisables (clauses),
 * documents et templates, par terme, portée, thématique, type de contrat et caractère normatif, avec facettes.
 */
@RestController
@ModuleRequis("redaction")
@RequestMapping("/api/search")
public class SearchController {

    private final RechercheMulticritere recherche;

    public SearchController(RechercheMulticritere recherche) {
        this.recherche = recherche;
    }

    @GetMapping
    public RechercheResultat rechercher(
            @RequestParam("q") String q,
            @RequestParam(value = "categorie", required = false) Enums.ClauseCategorie categorie,
            @RequestParam(value = "type", required = false) Enums.TypeContrat type,
            @RequestParam(value = "thematique", required = false) String thematique,
            @RequestParam(value = "normative", defaultValue = "false") boolean normative) {
        var criteres = new CriteresRechercheClause(q, categorie, type, thematique, normative);
        return recherche.rechercher(TenantContext.get(), q, criteres);
    }

    @GetMapping("/thematiques")
    public List<String> thematiques() {
        return recherche.thematiques(TenantContext.get());
    }
}
