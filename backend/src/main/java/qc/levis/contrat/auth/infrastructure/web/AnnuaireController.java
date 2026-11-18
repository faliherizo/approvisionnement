package qc.levis.contrat.auth.infrastructure.web;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import qc.levis.contrat.auth.application.AnnuaireService;
import qc.levis.contrat.auth.application.ScimService;
import qc.levis.contrat.auth.application.dto.CorrespondanceRequest;
import qc.levis.contrat.auth.application.dto.CorrespondanceVue;
import qc.levis.contrat.auth.application.dto.JetonScimVue;
import qc.levis.contrat.commun.infrastructure.TenantContext;

/** Correspondances d'annuaire et jeton SCIM de l'organisation (addenda 3, Q43, Q44, Q51) : administration seulement. */
@RestController
@RequestMapping("/api/admin/annuaire")
@PreAuthorize("hasRole('ADMIN')")
public class AnnuaireController {

    static final String POINT_SCIM = "/scim/v2";

    private final AnnuaireService annuaire;
    private final ScimService scim;

    public AnnuaireController(AnnuaireService annuaire, ScimService scim) {
        this.annuaire = annuaire;
        this.scim = scim;
    }

    @GetMapping("/correspondances")
    public List<CorrespondanceVue> correspondances() {
        return annuaire.correspondances(TenantContext.get());
    }

    @PostMapping("/correspondances")
    @ResponseStatus(HttpStatus.CREATED)
    public CorrespondanceVue ajouter(@RequestBody CorrespondanceRequest req, Authentication auth) {
        return annuaire.ajouter(TenantContext.get(), req, auth.getName());
    }

    @DeleteMapping("/correspondances/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id, Authentication auth) {
        annuaire.supprimer(TenantContext.get(), id, auth.getName());
    }

    @GetMapping("/scim")
    public JetonScimVue scim() {
        return scim.etatJeton(TenantContext.get(), POINT_SCIM);
    }

    /** Nouveau jeton, remis une seule fois ; le précédent est révoqué. */
    @PostMapping("/scim/jeton")
    public JetonScimVue jeton(Authentication auth) {
        return scim.creerJeton(TenantContext.get(), auth.getName(), POINT_SCIM);
    }
}
