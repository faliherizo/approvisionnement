package qc.levis.contrat.clm.infrastructure.web;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import qc.levis.contrat.clm.application.DemandesSignatureService;
import qc.levis.contrat.clm.application.dto.ClmDto;

/** Signature en lot initiée depuis la plateforme (addenda 1, AF-47) : mêmes profils que la création d'une trousse. */
@RestController
@RequestMapping("/api/clm/lots")
@PreAuthorize("@modules.actif('clm') and hasAnyRole('REDACTEUR','APPRO','JURIDIQUE','DIRECTION','ADMIN','SUPERADMIN')")
public class LotController {

    private final DemandesSignatureService demandes;
    private final WebClm.Acteurs acteurs;

    public LotController(DemandesSignatureService demandes, WebClm.Acteurs acteurs) {
        this.demandes = demandes;
        this.acteurs = acteurs;
    }

    @GetMapping
    public List<ClmDto.LotVue> lister() {
        return demandes.listerLots();
    }

    @GetMapping("/{id}")
    public ClmDto.LotVue lire(@PathVariable Long id) {
        return demandes.lot(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClmDto.LotVue creer(@RequestBody ClmDto.DemandeLot lot, Authentication auth) {
        return demandes.creerLot(lot, acteurs.de(auth));
    }
}
