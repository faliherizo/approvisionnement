package qc.levis.contrat.processus.infrastructure.web;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import qc.levis.contrat.processus.application.ProcessusService;
import qc.levis.contrat.processus.application.dto.DecisionRequest;
import qc.levis.contrat.processus.application.dto.DecisionVue;
import qc.levis.contrat.processus.application.dto.TacheVue;

/**
 * Tâches et approbations (écran 7). Gardé par le module « processus ». « Mes tâches » = tâches ouvertes
 * assignées à l'un des rôles de l'utilisateur ; la décision fait avancer le workflow.
 */
@RestController
@RequestMapping("/api/taches")
@PreAuthorize("@modules.actif('processus')")
public class TacheController {

    private final ProcessusService service;

    public TacheController(ProcessusService service) {
        this.service = service;
    }

    @GetMapping("/mine")
    public List<TacheVue> mesTaches(Authentication auth) {
        return service.mesTaches(roles(auth));
    }

    @PostMapping("/{id}/decisions")
    @PreAuthorize("@modules.actif('processus') and hasAnyRole('ADMIN','APPRO','DIRECTION','SUPERADMIN')")
    public void decider(@PathVariable Long id, @RequestBody DecisionRequest req, Authentication auth) {
        service.decider(id, roles(auth), auth != null ? auth.getName() : null, req);
    }

    @GetMapping("/demande/{saisieId}/historique")
    public List<DecisionVue> historique(@PathVariable Long saisieId) {
        return service.historique(saisieId);
    }

    /** Rôles de l'utilisateur sans le préfixe Spring « ROLE_ », pour correspondre aux rôles d'étape. */
    private List<String> roles(Authentication auth) {
        if (auth == null) return List.of();
        return auth.getAuthorities().stream()
                .map(a -> a.getAuthority().replaceFirst("^ROLE_", ""))
                .toList();
    }
}
