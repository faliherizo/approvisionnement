package qc.levis.contrat.interop.infrastructure.web;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.interop.application.InteropService;
import qc.levis.contrat.interop.application.dto.IdentifiantsClient;
import qc.levis.contrat.interop.domain.ApiClient;

/** Gestion des clients d'API (création des identifiants). Gardé par le module « interop ». */
@RestController
@RequestMapping("/api/interop/clients")
@PreAuthorize("@modules.actif('interop') and hasAnyRole('ADMIN','SUPERADMIN')")
public class InteropAdminController {

    private final InteropService service;

    public InteropAdminController(InteropService service) {
        this.service = service;
    }

    @GetMapping
    public List<ApiClient> lister() {
        return service.listerClients(TenantContext.get());
    }

    /** Crée un client et retourne le secret UNE SEULE FOIS. */
    @PostMapping
    public IdentifiantsClient creer(@RequestParam String nom, @RequestParam(required = false) String scopes) {
        return service.creerClient(TenantContext.get(), nom, scopes);
    }
}
