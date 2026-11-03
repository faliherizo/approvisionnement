package qc.levis.contrat.regle.infrastructure.web;

import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import qc.levis.contrat.regle.application.RegleService;
import qc.levis.contrat.regle.application.ResultatEvaluation;

/** Évaluation des règles d'un formulaire (visibilité/obligation/validation/calcul/routage). */
@RestController
@RequestMapping("/api/formulaires/{id}/regles")
@PreAuthorize("@modules.actif('regle')")
public class RegleController {

    private final RegleService service;

    public RegleController(RegleService service) {
        this.service = service;
    }

    /** Évalue les règles contre un jeu de réponses (appelé par le rendu dynamique, revalidé au serveur). */
    @PostMapping("/evaluer")
    public ResultatEvaluation evaluer(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> valeurs) {
        return service.evaluer(id, valeurs);
    }
}
