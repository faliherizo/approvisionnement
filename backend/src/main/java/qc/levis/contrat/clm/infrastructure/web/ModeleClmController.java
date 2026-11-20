package qc.levis.contrat.clm.infrastructure.web;

import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import qc.levis.contrat.clm.application.ModeleService;
import qc.levis.contrat.clm.application.dto.ClmDto;

/** Catalogue et éditeur de modèles (UI-07 à UI-09). */
@RestController
@RequestMapping("/api/clm/modeles")
@PreAuthorize("@modules.actif('clm')")
public class ModeleClmController {

    private final ModeleService modeles;
    private final WebClm.Acteurs acteurs;

    public ModeleClmController(ModeleService modeles, WebClm.Acteurs acteurs) {
        this.modeles = modeles;
        this.acteurs = acteurs;
    }

    @GetMapping
    public List<ClmDto.ModeleVue> lister(@RequestParam(required = false) String statut, Authentication auth) {
        return modeles.lister(acteurs.de(auth), statut);
    }

    @GetMapping("/{id}")
    public ClmDto.ModeleVue lire(@PathVariable Long id, Authentication auth) {
        return modeles.lire(id, acteurs.de(auth));
    }

    @PostMapping
    @PreAuthorize("@modules.actif('clm') and hasAnyRole('REDACTEUR','JURIDIQUE','ADMIN','ADMIN_AFFAIRES','SUPERADMIN')")
    public ClmDto.ModeleVue creer(@RequestBody ClmDto.ModeleRequest req, Authentication auth) {
        return modeles.creer(req, acteurs.de(auth));
    }

    @PutMapping("/{id}")
    public ClmDto.ModeleVue modifier(@PathVariable Long id, @RequestBody ClmDto.ModeleRequest req, Authentication auth) {
        return modeles.modifier(id, req, acteurs.de(auth));
    }

    @GetMapping("/{id}/validation")
    public ClmDto.Validation valider(@PathVariable Long id, Authentication auth) {
        return modeles.valider(id, acteurs.de(auth));
    }

    @PostMapping("/{id}/apercu")
    public ClmDto.ApercuModele apercu(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> valeurs, Authentication auth) {
        return modeles.apercu(id, valeurs, acteurs.de(auth));
    }

    @PostMapping("/{id}/soumettre")
    public ClmDto.ModeleVue soumettre(@PathVariable Long id, Authentication auth) {
        return modeles.soumettre(id, acteurs.de(auth));
    }

    @PostMapping("/{id}/renvoyer")
    public ClmDto.ModeleVue renvoyer(@PathVariable Long id, Authentication auth) {
        return modeles.renvoyer(id, acteurs.de(auth));
    }

    @PostMapping("/{id}/publier")
    public ClmDto.ModeleVue publier(@PathVariable Long id, Authentication auth) {
        return modeles.publier(id, acteurs.de(auth));
    }

    @PostMapping("/{id}/retirer")
    public ClmDto.ModeleVue retirer(@PathVariable Long id, Authentication auth) {
        return modeles.retirer(id, acteurs.de(auth));
    }

    @PostMapping("/{id}/nouvelle-version")
    public ClmDto.ModeleVue nouvelleVersion(@PathVariable Long id, Authentication auth) {
        return modeles.nouvelleVersion(id, acteurs.de(auth));
    }
}
