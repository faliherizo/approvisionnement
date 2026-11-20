package qc.levis.contrat.auth.infrastructure.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import qc.levis.contrat.auth.application.PolitiqueConnexionService;
import qc.levis.contrat.auth.application.dto.PolitiqueConnexionVue;
import qc.levis.contrat.commun.infrastructure.TenantContext;

/** Connexion locale et comptes de secours de l'organisation (addenda 4, Q12) : administration technique. */
@RestController
@RequestMapping("/api/admin/connexion-locale")
@PreAuthorize("hasRole('ADMIN_TECHNIQUE')")
public class ConnexionLocaleController {

    private final PolitiqueConnexionService politique;

    public ConnexionLocaleController(PolitiqueConnexionService politique) {
        this.politique = politique;
    }

    @GetMapping
    public PolitiqueConnexionVue etat() {
        return politique.etat(TenantContext.get());
    }

    @PutMapping
    public PolitiqueConnexionVue reserver(@RequestParam boolean reservee, Authentication auth) {
        return politique.reserver(TenantContext.get(), reservee, auth.getName());
    }

    @PutMapping("/comptes/{id}")
    public PolitiqueConnexionVue designer(@PathVariable Long id, @RequestParam boolean secours, Authentication auth) {
        return politique.designer(TenantContext.get(), id, secours, auth.getName());
    }
}
