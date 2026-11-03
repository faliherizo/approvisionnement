package qc.levis.contrat.securite.infrastructure.web;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.securite.application.SecuriteService;
import qc.levis.contrat.securite.domain.Groupe;
import qc.levis.contrat.securite.domain.GroupeMembre;
import qc.levis.contrat.securite.domain.PermissionAcces;

/** Gestion de la sécurité fine (permissions granulaires, groupes). Gardé par le module « securite_avancee ». */
@RestController
@RequestMapping("/api/securite")
@PreAuthorize("@modules.actif('securite_avancee') and hasAnyRole('ADMIN','SUPERADMIN')")
public class SecuriteController {

    private final SecuriteService service;

    public SecuriteController(SecuriteService service) {
        this.service = service;
    }

    @GetMapping("/permissions")
    public List<PermissionAcces> permissions() {
        return service.listerPermissions(TenantContext.get());
    }

    @PostMapping("/permissions")
    public PermissionAcces ajouterPermission(@RequestBody PermissionAcces p) {
        p.setTenantId(TenantContext.get());
        return service.ajouterPermission(p);
    }

    @DeleteMapping("/permissions/{id}")
    public void supprimerPermission(@PathVariable Long id) {
        service.supprimerPermission(id);
    }

    @GetMapping("/groupes")
    public List<Groupe> groupes() {
        return service.listerGroupes(TenantContext.get());
    }

    @PostMapping("/groupes")
    public Groupe ajouterGroupe(@RequestBody Groupe g) {
        g.setTenantId(TenantContext.get());
        return service.ajouterGroupe(g);
    }

    @PostMapping("/groupes/{code}/membres")
    public GroupeMembre ajouterMembre(@PathVariable String code, @RequestParam String utilisateur) {
        return service.ajouterMembre(new GroupeMembre(TenantContext.get(), code, utilisateur));
    }
}
