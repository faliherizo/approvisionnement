package qc.levis.contrat.vieprivee.infrastructure.web;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import qc.levis.contrat.vieprivee.application.DroitsVieePriveeService;
import qc.levis.contrat.vieprivee.application.dto.DemandeAccesRequest;
import qc.levis.contrat.vieprivee.application.dto.DemandeEffacementRequest;
import qc.levis.contrat.vieprivee.domain.BilanEffacement;
import qc.levis.contrat.vieprivee.domain.DemandeVieePrivee;
import qc.levis.contrat.vieprivee.domain.DossierRenseignements;

/**
 * Droits Loi 25 de la personne concernée (Lévis ENF1, C-15), exercés par l'administration de l'organisation, responsable
 * de la protection des renseignements personnels : accès, effacement et registre des demandes.
 */
@RestController
@RequestMapping("/api/loi25")
@PreAuthorize("hasRole('ADMIN')")
public class DroitsVieePriveeController {

    private final DroitsVieePriveeService service;

    public DroitsVieePriveeController(DroitsVieePriveeService service) {
        this.service = service;
    }

    @PostMapping("/acces")
    public DossierRenseignements acces(@Valid @RequestBody DemandeAccesRequest req, Authentication auth) {
        return service.acces(req.courriel(), auth.getName());
    }

    @PostMapping("/effacement")
    public List<BilanEffacement> effacer(@Valid @RequestBody DemandeEffacementRequest req, Authentication auth) {
        return service.effacer(req.courriel(), req.motif(), auth.getName());
    }

    @GetMapping("/demandes")
    public List<DemandeVieePrivee> registre() {
        return service.registre();
    }
}
