package qc.levis.contrat.interop.infrastructure.web;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import qc.levis.contrat.commun.module.application.ModuleService;
import qc.levis.contrat.interop.application.EchangesHeritesService;
import qc.levis.contrat.interop.application.InteropService;
import qc.levis.contrat.interop.application.dto.ContexteApi;
import qc.levis.contrat.interop.application.dto.EchangeDto;
import qc.levis.contrat.interop.application.port.FichesEchange;

/**
 * Échanges hérités sur l'API publique (CHU-8.04, ADR-021) : un système de la plateforme précédente écrit et lit des
 * fiches sous ses anciens noms de champs ; seuls l'URL et le jeton changent pour lui.
 */
@RestController
@RequestMapping("/api/public/v1/echanges/{code}/fiches")
public class EchangesHeritesPublicController {

    private final InteropService interop;
    private final ModuleService modules;
    private final EchangesHeritesService echanges;

    public EchangesHeritesPublicController(InteropService interop, ModuleService modules, EchangesHeritesService echanges) {
        this.interop = interop;
        this.modules = modules;
        this.echanges = echanges;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FichesEchange.FicheDeposee deposer(@PathVariable String code, @RequestHeader(value = "Authorization", required = false) String authz,
                                              @RequestBody Map<String, Object> enregistrement) {
        ContexteApi ctx = contexte(authz);
        return echanges.deposer(ctx.tenantId(), code, ctx.clientId(), enregistrement);
    }

    @GetMapping
    public List<EchangeDto.FicheHeritee> lire(@PathVariable String code, @RequestHeader(value = "Authorization", required = false) String authz,
                                              @RequestParam(required = false) Instant depuis) {
        return echanges.lire(contexte(authz).tenantId(), code, depuis);
    }

    /** Jeton d'API valide, puis module « interop » souscrit par son organisation. */
    private ContexteApi contexte(String authz) {
        String jeton = authz != null && authz.startsWith("Bearer ") ? authz.substring(7) : null;
        ContexteApi ctx = interop.authentifier(jeton)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide ou expiré."));
        if (!modules.estActif(ctx.tenantId(), "interop")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Module interop inactif.");
        }
        return ctx;
    }
}
