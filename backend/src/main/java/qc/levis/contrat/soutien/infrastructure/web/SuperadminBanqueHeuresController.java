package qc.levis.contrat.soutien.infrastructure.web;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import qc.levis.contrat.soutien.application.BanqueHeuresService;
import qc.levis.contrat.soutien.application.dto.BanqueRequest;
import qc.levis.contrat.soutien.application.dto.BanqueVue;
import qc.levis.contrat.soutien.application.dto.MouvementRequest;

/** Banque d'heures tenue par le fournisseur (CHU 9.05) : ouverture, achats, consommations et corrections. */
@RestController
@RequestMapping("/api/superadmin/tenants/{tid}/banques-heures")
@PreAuthorize("hasRole('SUPERADMIN')")
public class SuperadminBanqueHeuresController {

    private final BanqueHeuresService service;

    public SuperadminBanqueHeuresController(BanqueHeuresService service) {
        this.service = service;
    }

    @GetMapping
    public List<BanqueVue> lister(@PathVariable Long tid) { return service.lister(tid); }

    @PostMapping
    public BanqueVue ouvrir(@PathVariable Long tid, @Valid @RequestBody BanqueRequest req, Authentication auth) {
        return service.ouvrir(tid, req, auth.getName());
    }

    @GetMapping("/{id}")
    public BanqueVue consulter(@PathVariable Long tid, @PathVariable Long id) { return service.consulter(tid, id); }

    @PostMapping("/{id}/mouvements")
    public BanqueVue inscrire(@PathVariable Long tid, @PathVariable Long id, @Valid @RequestBody MouvementRequest req, Authentication auth) {
        return service.inscrire(tid, id, req, auth.getName());
    }
}
