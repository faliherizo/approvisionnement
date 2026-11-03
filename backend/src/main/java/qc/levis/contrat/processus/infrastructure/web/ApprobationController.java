package qc.levis.contrat.processus.infrastructure.web;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import qc.levis.contrat.processus.application.ApprobationService;
import qc.levis.contrat.processus.application.dto.*;
import qc.levis.contrat.processus.domain.RegleApprobation;

/**
 * Gestionnaire d'approbations multi-niveaux (moteur Approbations). Gardé par le module « processus ».
 * Règles configurables, création de demandes, étapes assignées, décision par l'approbateur désigné.
 */
@RestController
@RequestMapping("/api/approbations")
@PreAuthorize("@modules.actif('processus')")
public class ApprobationController {

    private final ApprobationService service;

    public ApprobationController(ApprobationService service) {
        this.service = service;
    }

    @GetMapping("/regles")
    public List<RegleApprobation> regles(@RequestParam(required = false) String documentType) {
        return service.lesRegles(documentType);
    }

    @PostMapping("/regles")
    @PreAuthorize("@modules.actif('processus') and hasAnyRole('ADMIN','SUPERADMIN')")
    public RegleApprobation creerRegle(@RequestBody RegleApproRequest req) {
        return service.enregistrerRegle(req);
    }

    @GetMapping("/demandes")
    public List<DemandeApproVue> demandes() {
        return service.toutesLesDemandes();
    }

    @PostMapping("/demandes")
    public DemandeApproVue creerDemande(@RequestBody DemandeApproRequest req, Authentication auth) {
        return service.creerDemande(req, auth != null ? auth.getName() : null);
    }

    @GetMapping("/demandes/{id}")
    public DemandeApproVue demande(@PathVariable Long id) {
        return service.demande(id);
    }

    @GetMapping("/mes-etapes")
    public List<EtapeApproVue> mesEtapes(Authentication auth) {
        return service.mesEtapes(roles(auth), auth != null ? auth.getName() : null);
    }

    @PostMapping("/etapes/{id}/decider")
    public DemandeApproVue decider(@PathVariable Long id, @RequestBody DecisionApproRequest req, Authentication auth) {
        return service.decider(id, roles(auth), auth != null ? auth.getName() : null, req);
    }

    private List<String> roles(Authentication auth) {
        if (auth == null) return List.of();
        return auth.getAuthorities().stream().map(a -> a.getAuthority().replaceFirst("^ROLE_", "")).toList();
    }
}
