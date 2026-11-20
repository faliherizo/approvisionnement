package qc.levis.contrat.clm.infrastructure.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import qc.levis.contrat.clm.application.DepartEmployeService;
import qc.levis.contrat.clm.application.dto.ClmDto;

/** Départ d'un employé (P3) : inventaire puis transfert, par l'administration d'affaires. */
@RestController
@RequestMapping("/api/clm/admin/departs")
@PreAuthorize("@modules.actif('clm') and hasRole('ADMIN_AFFAIRES')")
public class DepartEmployeController {

    private final DepartEmployeService departs;
    private final WebClm.Acteurs acteurs;

    public DepartEmployeController(DepartEmployeService departs, WebClm.Acteurs acteurs) {
        this.departs = departs;
        this.acteurs = acteurs;
    }

    @GetMapping
    public ClmDto.InventaireDepart inventaire(@RequestParam String courriel, Authentication auth) {
        return departs.inventaire(courriel, acteurs.de(auth));
    }

    @PostMapping
    public ClmDto.BilanDepart transferer(@RequestBody ClmDto.DepartRequest req, Authentication auth) {
        return departs.transferer(req, acteurs.de(auth));
    }
}
