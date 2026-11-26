package qc.levis.contrat.clm.infrastructure.web;

import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import qc.levis.contrat.clm.application.DepotsM365Service;
import qc.levis.contrat.clm.application.dto.ClmDto;

/**
 * Diffusion Microsoft 365 des trousses terminées (Annexe 10, TI-01) : réglage, suivi et relance des dépôts. Administration
 * technique seulement (addenda 4, Q10) ; la connexion Microsoft 365 se règle sur {@code /api/parametres/integration-m365}.
 */
@RestController
@RequestMapping("/api/clm/admin/integrations/m365")
@PreAuthorize("@modules.actif('clm') and hasRole('ADMIN_TECHNIQUE')")
public class Microsoft365ClmController {

    private final DepotsM365Service depots;

    public Microsoft365ClmController(DepotsM365Service depots) {
        this.depots = depots;
    }

    @GetMapping
    public ClmDto.DiffusionM365Vue configuration() {
        return depots.configuration();
    }

    @PutMapping
    public ClmDto.DiffusionM365Vue configurer(@RequestBody ClmDto.DiffusionM365Request req, Authentication auth) {
        return depots.configurer(req, auth.getName());
    }

    @GetMapping("/depots")
    public List<ClmDto.DepotM365Vue> lister() {
        return depots.lister();
    }

    @PostMapping("/depots/{id}/relancer")
    public ClmDto.DepotM365Vue relancer(@PathVariable Long id, Authentication auth) {
        return depots.relancer(id, auth.getName());
    }

    /** Livraison immédiate des dépôts dus, sans attendre le planificateur (après une panne réparée). */
    @PostMapping("/relayer")
    public Map<String, Integer> relayer() {
        return Map.of("deposes", depots.relayer());
    }
}
