package qc.levis.contrat.saisie.infrastructure.web;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import qc.levis.contrat.saisie.application.SaisieService;
import qc.levis.contrat.saisie.application.dto.SaisieVue;
import qc.levis.contrat.saisie.application.dto.SoumissionRequest;

/** API des fiches (soumissions). Gardée par le module « saisie ». */
@RestController
@RequestMapping("/api/formulaires/{fid}/saisies")
@PreAuthorize("@modules.actif('saisie')")
public class SaisieController {

    private final SaisieService service;

    public SaisieController(SaisieService service) {
        this.service = service;
    }

    /** Structure du formulaire pour le rendu côté répondant (gardée par le seul module « saisie »). */
    @GetMapping("/structure")
    public Map<String, String> structure(@PathVariable Long fid) {
        Map<String, String> m = new HashMap<>();
        m.put("structureJson", service.structure(fid));
        return m;
    }

    @PostMapping
    public SaisieVue creer(@PathVariable Long fid, Authentication auth) {
        return service.creer(fid, auth != null ? auth.getName() : null);
    }

    @PatchMapping("/{id}")
    public SaisieVue autosave(@PathVariable Long fid, @PathVariable Long id, @RequestBody(required = false) String donneesJson) {
        return service.autosave(id, donneesJson);
    }

    @PostMapping("/{id}/soumettre")
    public SaisieVue soumettre(@PathVariable Long fid, @PathVariable Long id,
                               @RequestBody(required = false) SoumissionRequest req, HttpServletRequest http) {
        String ip = http.getHeader("X-Forwarded-For");
        if (ip != null && ip.contains(",")) ip = ip.split(",")[0].trim();
        if (ip == null || ip.isBlank()) ip = http.getRemoteAddr();
        return service.soumettre(id, req != null ? req : new SoumissionRequest(null, null), ip);
    }

    @PostMapping("/{id}/prendre-en-charge")
    @PreAuthorize("@modules.actif('saisie') and hasAnyRole('ADMIN','APPRO','DIRECTION','SUPERADMIN')")
    public SaisieVue prendreEnCharge(@PathVariable Long fid, @PathVariable Long id) {
        return service.prendreEnCharge(id);
    }

    @PostMapping("/{id}/complement")
    @PreAuthorize("@modules.actif('saisie') and hasAnyRole('ADMIN','APPRO','DIRECTION','SUPERADMIN')")
    public SaisieVue demanderComplement(@PathVariable Long fid, @PathVariable Long id) {
        return service.demanderComplement(id);
    }

    @PostMapping("/{id}/valider")
    @PreAuthorize("@modules.actif('saisie') and hasAnyRole('ADMIN','APPRO','DIRECTION','SUPERADMIN')")
    public SaisieVue valider(@PathVariable Long fid, @PathVariable Long id) {
        return service.valider(id);
    }

    @PostMapping("/{id}/rejeter")
    @PreAuthorize("@modules.actif('saisie') and hasAnyRole('ADMIN','APPRO','DIRECTION','SUPERADMIN')")
    public SaisieVue rejeter(@PathVariable Long fid, @PathVariable Long id) {
        return service.rejeter(id);
    }

    @PostMapping("/{id}/cloturer")
    @PreAuthorize("@modules.actif('saisie') and hasAnyRole('ADMIN','APPRO','DIRECTION','SUPERADMIN')")
    public SaisieVue cloturer(@PathVariable Long fid, @PathVariable Long id) {
        return service.cloturer(id);
    }

    /** Annulation d'un brouillon par le demandeur (aucun rôle d'approbation requis). */
    @PostMapping("/{id}/annuler")
    public SaisieVue annuler(@PathVariable Long fid, @PathVariable Long id) {
        return service.annuler(id);
    }

    @GetMapping
    public List<SaisieVue> lister(@PathVariable Long fid) {
        return service.lister(fid);
    }

    @GetMapping("/{id}")
    public SaisieVue obtenir(@PathVariable Long fid, @PathVariable Long id) {
        return service.obtenir(id);
    }
}
