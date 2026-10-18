package qc.levis.contrat.reference.infrastructure.web;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import qc.levis.contrat.reference.application.ReferenceService;
import qc.levis.contrat.reference.application.dto.Option;

/** Lecture des options de sélecteurs (global + tenant courant), pour tout utilisateur authentifié. */
@RestController
@RequestMapping("/api/reference")
public class ReferenceController {

    private final ReferenceService service;

    public ReferenceController(ReferenceService service) {
        this.service = service;
    }

    @GetMapping("/{categorie}")
    public List<Option> options(@PathVariable String categorie) {
        return service.options(categorie);
    }
}
