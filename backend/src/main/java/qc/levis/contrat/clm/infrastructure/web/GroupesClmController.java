package qc.levis.contrat.clm.infrastructure.web;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import qc.levis.contrat.clm.application.GroupesClmService;
import qc.levis.contrat.clm.application.dto.ClmDto;

/** Groupes du module (UI-20, zone 03) : consultés par tout utilisateur du module, administrés par l'administrateur. */
@RestController
@RequestMapping("/api/clm")
@PreAuthorize("@modules.actif('clm')")
public class GroupesClmController {

    private static final String ADMIN = "@modules.actif('clm') and hasAnyRole('ADMIN_AFFAIRES')";

    private final GroupesClmService groupes;

    public GroupesClmController(GroupesClmService groupes) {
        this.groupes = groupes;
    }

    /** Liste servie aux éditeurs de modèles et de parcours (partage, affectation à un groupe). */
    @GetMapping("/groupes")
    public List<ClmDto.GroupeVue> lister() {
        return groupes.lister();
    }

    @PostMapping("/admin/groupes")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(ADMIN)
    public ClmDto.GroupeVue creer(@RequestBody ClmDto.GroupeRequest req, Authentication auth) {
        return groupes.creer(req, auth.getName());
    }

    /** Renommer un groupe : le libellé change, le code (repris par les parcours et les modèles) reste. */
    @PutMapping("/admin/groupes/{id}")
    @PreAuthorize(ADMIN)
    public ClmDto.GroupeVue renommer(@PathVariable Long id, @RequestBody ClmDto.GroupeRequest req, Authentication auth) {
        return groupes.renommer(id, req, auth.getName());
    }

    @DeleteMapping("/admin/groupes/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(ADMIN)
    public void supprimer(@PathVariable Long id, Authentication auth) {
        groupes.supprimer(id, auth.getName());
    }

    @PostMapping("/admin/groupes/{id}/membres")
    @PreAuthorize(ADMIN)
    public ClmDto.GroupeVue ajouterMembre(@PathVariable Long id, @RequestBody ClmDto.MembreRequest req, Authentication auth) {
        return groupes.ajouterMembre(id, req, auth.getName());
    }

    @DeleteMapping("/admin/groupes/{id}/membres/{courriel}")
    @PreAuthorize(ADMIN)
    public ClmDto.GroupeVue retirerMembre(@PathVariable Long id, @PathVariable String courriel, Authentication auth) {
        return groupes.retirerMembre(id, courriel, auth.getName());
    }
}
