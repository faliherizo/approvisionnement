package qc.levis.contrat.saisie.infrastructure.web;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import qc.levis.contrat.saisie.application.SaisieService;
import qc.levis.contrat.saisie.application.dto.DemandeVue;

/**
 * Portail « Mes demandes » (maquettes §4.1, écran 6) : les demandes de l'utilisateur courant, toutes
 * formes confondues. Gardé par le module « saisie ». Un utilisateur ne voit QUE ses propres demandes.
 */
@RestController
@RequestMapping("/api/mes-demandes")
@PreAuthorize("@modules.actif('saisie')")
public class MesDemandesController {

    private final SaisieService service;

    public MesDemandesController(SaisieService service) {
        this.service = service;
    }

    @GetMapping
    public List<DemandeVue> mesDemandes(Authentication auth) {
        return service.mesDemandes(auth != null ? auth.getName() : null);
    }
}
