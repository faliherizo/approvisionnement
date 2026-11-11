package qc.levis.contrat.clm.infrastructure.web;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import qc.levis.contrat.clm.application.ClientsIntegrationService;
import qc.levis.contrat.clm.application.RappelsExternesService;
import qc.levis.contrat.clm.application.dto.ClmDto;

/** Administration des clients d'intégration entrants et de leurs rappels (UI-21, addenda 1). */
@RestController
@RequestMapping("/api/clm/admin")
@PreAuthorize("@modules.actif('clm') and hasAnyRole('ADMIN','SUPERADMIN')")
public class IntegrationsEntrantesAdminController {

    private final ClientsIntegrationService clients;
    private final RappelsExternesService rappels;

    public IntegrationsEntrantesAdminController(ClientsIntegrationService clients, RappelsExternesService rappels) {
        this.clients = clients;
        this.rappels = rappels;
    }

    @GetMapping("/clients-integration")
    public List<ClmDto.ClientIntegrationVue> lister() {
        return clients.lister();
    }

    @PostMapping("/clients-integration")
    @ResponseStatus(HttpStatus.CREATED)
    public ClmDto.ClientIntegrationCree creer(@RequestBody ClmDto.ClientIntegrationRequest req, Authentication auth) {
        return clients.creer(req, auth.getName());
    }

    @PostMapping("/clients-integration/{id}/revoquer")
    public ClmDto.ClientIntegrationVue revoquer(@PathVariable Long id, Authentication auth) {
        return clients.revoquer(id, auth.getName());
    }

    @GetMapping("/rappels")
    public List<ClmDto.RappelVue> rappels() {
        return rappels.lister();
    }

    @PostMapping("/rappels/{id}/relancer")
    public ClmDto.RappelVue relancer(@PathVariable Long id) {
        return rappels.relancer(id);
    }
}
