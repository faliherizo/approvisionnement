package qc.levis.contrat.clm.infrastructure.web;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import qc.levis.contrat.clm.application.TachesAssigneesService;
import qc.levis.contrat.clm.application.dto.ClmDto;

/** Tâches confiées à une personne (boîte de tâches, UI-12) : création, suivi, réassignation. */
@RestController
@RequestMapping("/api/clm")
@PreAuthorize("@modules.actif('clm')")
public class TacheAssigneeController {

    /** Mêmes profils que la création d'une trousse : l'auditeur et le lecteur consultent seulement. */
    private static final String CREATION = "@modules.actif('clm') and hasAnyRole('REDACTEUR','APPRO','JURIDIQUE','DIRECTION','ADMIN','SUPERADMIN')";

    private final TachesAssigneesService taches;
    private final WebClm.Acteurs acteurs;

    public TacheAssigneeController(TachesAssigneesService taches, WebClm.Acteurs acteurs) {
        this.taches = taches;
        this.acteurs = acteurs;
    }

    @GetMapping("/membres")
    public List<ClmDto.MembreVue> membres() {
        return taches.membres();
    }

    @GetMapping("/taches-assignees")
    public List<ClmDto.TacheAssigneeVue> lister(@RequestParam(defaultValue = "assignees") String filtre, Authentication auth) {
        return taches.lister(acteurs.de(auth), filtre);
    }

    @GetMapping("/trousses/{id}/taches-assignees")
    public List<ClmDto.TacheAssigneeVue> deLaTrousse(@PathVariable Long id, Authentication auth) {
        return taches.deLaTrousse(id, acteurs.de(auth));
    }

    @PostMapping("/taches-assignees")
    @PreAuthorize(CREATION)
    public ClmDto.TacheAssigneeVue creer(@RequestBody ClmDto.TacheAssigneeRequest req, Authentication auth) {
        return taches.creer(acteurs.de(auth), req);
    }

    @PostMapping("/taches-assignees/{id}/action")
    public ClmDto.TacheAssigneeVue agir(@PathVariable Long id, @RequestBody ClmDto.ActionTacheRequest req, Authentication auth) {
        return taches.agir(id, acteurs.de(auth), req);
    }

    @PostMapping("/taches-assignees/{id}/reassigner")
    public ClmDto.TacheAssigneeVue reassigner(@PathVariable Long id, @RequestBody ClmDto.DelegationRequest req, Authentication auth) {
        return taches.reassigner(id, acteurs.de(auth), req);
    }
}
