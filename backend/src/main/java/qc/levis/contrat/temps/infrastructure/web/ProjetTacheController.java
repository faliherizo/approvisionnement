package qc.levis.contrat.temps.infrastructure.web;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import qc.levis.contrat.temps.application.ProjetTacheService;
import qc.levis.contrat.temps.application.dto.*;

/**
 * Projets et tâches (Espace RH) : création de tâches, assignation à une personne, suivi. Gardé par le
 * module « rh ». La gestion (créer/assigner) exige un rôle d'administration ; chacun voit « mes tâches ».
 */
@RestController
@RequestMapping("/api/rh")
@PreAuthorize("@modules.actif('rh')")
public class ProjetTacheController {

    private final ProjetTacheService service;

    public ProjetTacheController(ProjetTacheService service) {
        this.service = service;
    }

    @GetMapping("/projets")
    public List<ProjetVue> projets() {
        return service.listerProjets();
    }

    @PostMapping("/projets")
    @PreAuthorize("@modules.actif('rh') and hasAnyRole('ADMIN','DIRECTION','APPRO','SUPERADMIN')")
    public ProjetVue creerProjet(@RequestBody ProjetRequest req) {
        return service.creerProjet(req);
    }

    @PutMapping("/projets/{id}")
    @PreAuthorize("@modules.actif('rh') and hasAnyRole('ADMIN','DIRECTION','APPRO','SUPERADMIN')")
    public ProjetVue modifierProjet(@PathVariable Long id, @RequestBody ProjetRequest req) {
        return service.modifierProjet(id, req);
    }

    @GetMapping("/taches")
    public List<TacheProjetVue> taches(@RequestParam(required = false) Long projetId,
                                       @RequestParam(required = false) Long assigneId) {
        return service.listerTaches(projetId, assigneId);
    }

    @GetMapping("/taches/mine")
    public List<TacheProjetVue> mesTaches(Authentication auth) {
        return service.mesTaches(auth != null ? auth.getName() : null);
    }

    @PostMapping("/taches")
    @PreAuthorize("@modules.actif('rh') and hasAnyRole('ADMIN','DIRECTION','APPRO','SUPERADMIN')")
    public TacheProjetVue creerTache(@RequestBody TacheProjetRequest req) {
        return service.creerTache(req);
    }

    @PutMapping("/taches/{id}")
    @PreAuthorize("@modules.actif('rh') and hasAnyRole('ADMIN','DIRECTION','APPRO','SUPERADMIN','REDACTEUR')")
    public TacheProjetVue modifierTache(@PathVariable Long id, @RequestBody TacheProjetRequest req) {
        return service.modifierTache(id, req);
    }

    @DeleteMapping("/taches/{id}")
    @PreAuthorize("@modules.actif('rh') and hasAnyRole('ADMIN','DIRECTION','APPRO','SUPERADMIN')")
    public void supprimerTache(@PathVariable Long id) {
        service.supprimerTache(id);
    }
}
