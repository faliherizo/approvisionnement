package qc.levis.contrat.clm.infrastructure.web;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import qc.levis.contrat.clm.application.PortailNegociationService;
import qc.levis.contrat.clm.application.dto.NegociationDto;
import qc.levis.contrat.commun.module.application.ModuleService;

/**
 * Portail externe, volet négociation (RST-55) : la contrepartie n'a pas de compte, le jeton du lien est sa seule capacité.
 * L'organisation n'est connue qu'à la lecture du jeton ; l'option « clm_negociation » est alors exigée pour elle.
 */
@RestController
@RequestMapping("/api/clm/portail/negociation/{jeton}")
public class PortailNegociationController {

    static final String OPTION = "clm_negociation";

    private final PortailNegociationService portail;
    private final ModuleService modules;

    public PortailNegociationController(PortailNegociationService portail, ModuleService modules) {
        this.portail = portail;
        this.modules = modules;
    }

    @GetMapping
    public NegociationDto.PortailVue consulter(@PathVariable String jeton) {
        exigerOption(jeton);
        return portail.consulter(jeton);
    }

    @GetMapping("/comparaison")
    public NegociationDto.ComparaisonVue comparer(@PathVariable String jeton, @RequestParam int de, @RequestParam int a) {
        exigerOption(jeton);
        return portail.comparer(jeton, de, a);
    }

    @PostMapping("/propositions")
    public NegociationDto.PortailVue proposer(@PathVariable String jeton, @RequestBody NegociationDto.PropositionsRequest req) {
        exigerOption(jeton);
        return portail.proposer(jeton, req);
    }

    @PostMapping("/acceptation")
    public NegociationDto.PortailVue accepter(@PathVariable String jeton) {
        exigerOption(jeton);
        return portail.accepter(jeton);
    }

    private void exigerOption(String jeton) {
        if (!modules.estActif(portail.organisation(jeton), OPTION)) {
            throw new AccessDeniedException("Négociation indisponible : l'option n'est pas souscrite par l'organisation.");
        }
    }
}
