package qc.levis.contrat.formulaire.infrastructure.web;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import qc.levis.contrat.formulaire.application.FormulaireService;
import qc.levis.contrat.formulaire.application.dto.FormulaireRequest;
import qc.levis.contrat.formulaire.application.dto.FormulaireVue;
import qc.levis.contrat.formulaire.application.dto.StructureRequest;
import qc.levis.contrat.formulaire.application.dto.VersionVue;

/**
 * API du moteur de formulaires. Accès gardé par le MODULE « formulaire » (refus par défaut si le module
 * n'est pas activé pour le tenant) ET par rôle.
 */
@RestController
@RequestMapping("/api/formulaires")
@PreAuthorize("@modules.actif('formulaire') and hasAnyRole('ADMIN','REDACTEUR','SUPERADMIN')")
public class FormulaireController {

    private final FormulaireService service;

    public FormulaireController(FormulaireService service) {
        this.service = service;
    }

    @GetMapping
    public List<FormulaireVue> lister(@RequestParam(required = false) String domaine) {
        return service.lister(domaine);
    }

    @PostMapping
    public FormulaireVue creer(@Valid @RequestBody FormulaireRequest req) {
        return service.creer(req);
    }

    @GetMapping("/{id}/version")
    public VersionVue versionCourante(@PathVariable Long id) {
        return service.versionCourante(id);
    }

    @GetMapping("/{id}/historique")
    public List<VersionVue> historique(@PathVariable Long id) {
        return service.historique(id);
    }

    @PutMapping("/{id}/structure")
    public VersionVue majStructure(@PathVariable Long id, @RequestBody StructureRequest req) {
        return service.majStructure(id, req);
    }

    @PostMapping("/{id}/soumettre")
    public VersionVue soumettre(@PathVariable Long id) {
        return service.soumettre(id);
    }

    @PostMapping("/{id}/publier")
    public VersionVue publier(@PathVariable Long id) {
        return service.publier(id);
    }

    @PostMapping("/{id}/revenir/{numero}")
    public VersionVue revenir(@PathVariable Long id, @PathVariable int numero) {
        return service.revenirA(id, numero);
    }

    @PostMapping("/{id}/dupliquer")
    public FormulaireVue dupliquer(@PathVariable Long id, @RequestParam String code, @RequestParam String nom) {
        return service.dupliquer(id, code, nom);
    }

    @GetMapping("/{id}/export")
    public String exporter(@PathVariable Long id) {
        return service.exporter(id);
    }

    @PostMapping("/importer")
    public FormulaireVue importer(@RequestParam String code, @RequestParam String nom, @RequestBody(required = false) String structureJson) {
        return service.importer(code, nom, structureJson);
    }

    @DeleteMapping("/{id}")
    public void supprimer(@PathVariable Long id) {
        service.supprimer(id);
    }
}
