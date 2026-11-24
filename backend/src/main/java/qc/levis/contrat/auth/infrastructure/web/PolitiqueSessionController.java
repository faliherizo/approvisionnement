package qc.levis.contrat.auth.infrastructure.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import qc.levis.contrat.auth.application.PolitiqueSessionService;
import qc.levis.contrat.auth.application.dto.PolitiqueSessionVue;
import qc.levis.contrat.commun.infrastructure.TenantContext;

/** Politique de session de l'organisation (ST-01) : délai d'inactivité et durée maximale, fixés par l'administration technique. */
@RestController
@RequestMapping("/api/admin/session")
@PreAuthorize("hasRole('ADMIN_TECHNIQUE')")
public class PolitiqueSessionController {

    private final PolitiqueSessionService politique;

    public PolitiqueSessionController(PolitiqueSessionService politique) {
        this.politique = politique;
    }

    @GetMapping
    public PolitiqueSessionVue etat() {
        return politique.etat(TenantContext.get());
    }

    @PutMapping
    public PolitiqueSessionVue definir(@RequestParam long inactiviteMinutes, @RequestParam long dureeMaxMinutes, Authentication auth) {
        return politique.definir(TenantContext.get(), inactiviteMinutes, dureeMaxMinutes, auth.getName());
    }
}
