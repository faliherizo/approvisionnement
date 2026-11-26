package qc.levis.contrat.clm.infrastructure.web;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import qc.levis.contrat.clm.application.AssistanceNegociationService;
import qc.levis.contrat.clm.application.NegociationService;
import qc.levis.contrat.clm.application.OuvertureNegociationService;
import qc.levis.contrat.clm.application.dto.NegociationDto;

/**
 * Négociation assistée avec un tiers, côté organisation (RST-55, IA-05) : option du module CLM, souscrite à part
 * (« clm_negociation »). Les droits fins (gestion de la trousse) sont vérifiés par les cas d'utilisation.
 */
@RestController
@RequestMapping("/api/clm")
@PreAuthorize("@modules.actif('clm_negociation')")
public class NegociationController {

    private static final String GESTION = "@modules.actif('clm_negociation') and hasAnyRole('REDACTEUR','APPRO','JURIDIQUE','DIRECTION','ADMIN','ADMIN_AFFAIRES')";

    private final OuvertureNegociationService ouverture;
    private final NegociationService negociations;
    private final AssistanceNegociationService assistance;
    private final WebClm.Acteurs acteurs;

    public NegociationController(OuvertureNegociationService ouverture, NegociationService negociations,
                                 AssistanceNegociationService assistance, WebClm.Acteurs acteurs) {
        this.ouverture = ouverture;
        this.negociations = negociations;
        this.assistance = assistance;
        this.acteurs = acteurs;
    }

    @GetMapping("/trousses/{id}/negociations")
    public List<NegociationDto.NegociationVue> lister(@PathVariable Long id, Authentication auth) {
        return negociations.lister(id, acteurs.de(auth));
    }

    @PostMapping("/trousses/{id}/negociations")
    @PreAuthorize(GESTION)
    public NegociationDto.NegociationVue ouvrir(@PathVariable Long id, @RequestBody NegociationDto.OuvertureRequest req, Authentication auth) {
        return ouverture.ouvrir(id, req, acteurs.de(auth));
    }

    @GetMapping("/negociations/{id}/comparaison")
    public NegociationDto.ComparaisonVue comparer(@PathVariable Long id, @RequestParam int de, @RequestParam int a, Authentication auth) {
        return negociations.comparer(id, de, a, acteurs.de(auth));
    }

    @PostMapping("/negociations/{id}/suggestions")
    @PreAuthorize(GESTION)
    public NegociationDto.NegociationVue suggerer(@PathVariable Long id, Authentication auth) {
        return assistance.suggerer(id, acteurs.de(auth));
    }

    @PostMapping("/negociations/{id}/decisions")
    @PreAuthorize(GESTION)
    public NegociationDto.NegociationVue decider(@PathVariable Long id, @RequestBody NegociationDto.DecisionsRequest req, Authentication auth) {
        return negociations.decider(id, req, acteurs.de(auth));
    }

    @PostMapping("/negociations/{id}/cloture")
    @PreAuthorize(GESTION)
    public NegociationDto.NegociationVue clore(@PathVariable Long id, Authentication auth) {
        return negociations.clore(id, acteurs.de(auth));
    }

    @PostMapping("/negociations/{id}/annulation")
    @PreAuthorize(GESTION)
    public NegociationDto.NegociationVue annuler(@PathVariable Long id, @RequestBody NegociationDto.AnnulationRequest req, Authentication auth) {
        return negociations.annuler(id, req, acteurs.de(auth));
    }
}
