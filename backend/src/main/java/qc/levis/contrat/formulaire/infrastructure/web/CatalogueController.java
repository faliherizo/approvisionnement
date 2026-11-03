package qc.levis.contrat.formulaire.infrastructure.web;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import qc.levis.contrat.formulaire.application.FormulaireService;
import qc.levis.contrat.formulaire.application.dto.FormulaireVue;

/**
 * Catalogue des formulaires publiés, pour le PORTAIL répondant (« Nouveau formulaire », maquettes §4.1).
 * Gardé par le module « formulaire » sans restriction de rôle : tout utilisateur authentifié du tenant
 * voit les formulaires publiés (filtrables par domaine), sans accéder à l'API de conception.
 */
@RestController
@RequestMapping("/api/catalogue")
@PreAuthorize("@modules.actif('formulaire')")
public class CatalogueController {

    private final FormulaireService service;

    public CatalogueController(FormulaireService service) {
        this.service = service;
    }

    @GetMapping
    public List<FormulaireVue> catalogue(@RequestParam(required = false) String domaine) {
        return service.catalogue(domaine);
    }
}
