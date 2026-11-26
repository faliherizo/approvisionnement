package qc.levis.contrat.clm.infrastructure.web;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import qc.levis.contrat.clm.application.EtiquetagePurviewService;
import qc.levis.contrat.clm.application.dto.ClmDto;

/**
 * Étiquettes Microsoft Purview du module (Annexe 10, SEC-27 ; ADR-036) : catalogue du locataire Microsoft 365 et politiques
 * d'étiquetage, par défaut et par type de contrat. Administration d'affaires (conservation) ou technique (Microsoft 365).
 */
@RestController
@RequestMapping("/api/clm/admin/purview")
@PreAuthorize("@modules.actif('clm') and hasAnyRole('ADMIN_AFFAIRES', 'ADMIN_TECHNIQUE')")
public class PurviewClmController {

    private final EtiquetagePurviewService purview;

    public PurviewClmController(EtiquetagePurviewService purview) {
        this.purview = purview;
    }

    @GetMapping("/catalogue")
    public List<ClmDto.EtiquettePurviewVue> catalogue() {
        return purview.catalogue();
    }

    @GetMapping("/politiques")
    public List<ClmDto.PolitiquePurviewVue> politiques() {
        return purview.politiques();
    }

    @PutMapping("/politiques")
    public ClmDto.PolitiquePurviewVue definir(@RequestBody ClmDto.PolitiquePurviewRequest req, Authentication auth) {
        return purview.definir(req, auth.getName());
    }

    @DeleteMapping("/politiques/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id, Authentication auth) {
        purview.supprimer(id, auth.getName());
    }
}
