package qc.levis.contrat.rapport.infrastructure.web;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import qc.levis.contrat.rapport.application.TableauBordService;
import qc.levis.contrat.rapport.application.dto.TableauBordRequest;
import qc.levis.contrat.rapport.application.dto.TableauBordVue;

/** Tableaux de bord configurables (CHU 6.04), pour le personnel qui pilote le traitement des demandes. */
@RestController
@RequestMapping("/api/rapport/tableaux-de-bord")
@PreAuthorize("@modules.actif('rapport') and hasAnyRole('ADMIN','APPRO','DIRECTION','REDACTEUR')")
public class TableauBordController {

    private final TableauBordService service;

    public TableauBordController(TableauBordService service) {
        this.service = service;
    }

    @GetMapping
    public List<TableauBordVue> lister(Authentication auth) { return service.lister(auth.getName()); }

    @GetMapping("/{id}")
    public TableauBordVue consulter(@PathVariable Long id, Authentication auth) { return service.consulter(id, auth.getName()); }

    @PostMapping
    public TableauBordVue creer(@Valid @RequestBody TableauBordRequest req, Authentication auth) { return service.creer(req, auth.getName()); }

    @PutMapping("/{id}")
    public TableauBordVue modifier(@PathVariable Long id, @Valid @RequestBody TableauBordRequest req, Authentication auth) {
        return service.modifier(id, req, auth.getName());
    }

    @DeleteMapping("/{id}")
    public void supprimer(@PathVariable Long id, Authentication auth) { service.supprimer(id, auth.getName()); }
}
