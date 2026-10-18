package qc.levis.contrat.dossier.infrastructure.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import qc.levis.contrat.addenda.domain.Addenda;
import qc.levis.contrat.dossier.application.RapportService;
import qc.levis.contrat.dossier.application.dto.RapportSuivi;

/** Rapports de suivi consolidés (Addenda 1, exig. 22). Réservé aux rôles de pilotage. */
@RestController
@RequestMapping("/api/rapports")
public class RapportController {

    private final RapportService service;

    public RapportController(RapportService service) {
        this.service = service;
    }

    @GetMapping("/suivi")
    @PreAuthorize("hasAnyRole('APPRO','DIRECTION','ADMIN')")
    public RapportSuivi suivi() {
        return service.suivi();
    }
}
