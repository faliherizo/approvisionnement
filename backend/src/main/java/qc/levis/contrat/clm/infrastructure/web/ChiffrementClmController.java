package qc.levis.contrat.clm.infrastructure.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import qc.levis.contrat.clm.application.ChiffrementClmService;
import qc.levis.contrat.clm.application.dto.ClmDto;

/**
 * Chiffrement au repos par une clé dédiée à l'organisation (Annexe 10, SEC-28 ; ADR-037) : état, rotation et
 * réenveloppement. Administration technique seulement ; aucune clé n'est jamais exposée.
 */
@RestController
@RequestMapping("/api/clm/admin/chiffrement")
@PreAuthorize("@modules.actif('clm') and hasRole('ADMIN_TECHNIQUE')")
public class ChiffrementClmController {

    private final ChiffrementClmService chiffrement;

    public ChiffrementClmController(ChiffrementClmService chiffrement) {
        this.chiffrement = chiffrement;
    }

    @GetMapping
    public ClmDto.ChiffrementVue etat() {
        return chiffrement.etat();
    }

    @PostMapping("/pivoter")
    public ClmDto.ChiffrementVue pivoter(Authentication auth) {
        return chiffrement.pivoter(auth.getName());
    }

    @PostMapping("/reenvelopper")
    public ClmDto.ReenveloppementVue reenvelopper(Authentication auth) {
        return chiffrement.reenvelopper(auth.getName());
    }
}
