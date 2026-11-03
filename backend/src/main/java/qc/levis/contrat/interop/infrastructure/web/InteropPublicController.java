package qc.levis.contrat.interop.infrastructure.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.commun.module.application.ModuleService;
import qc.levis.contrat.formulaire.application.repository.FormulaireRepository;
import qc.levis.contrat.interop.application.InteropService;
import qc.levis.contrat.interop.application.dto.ContexteApi;
import qc.levis.contrat.interop.application.dto.JetonEmis;
import qc.levis.contrat.saisie.application.SaisieService;
import qc.levis.contrat.saisie.application.dto.SaisieVue;
import qc.levis.contrat.saisie.application.dto.SoumissionRequest;

/**
 * API PUBLIQUE tokenisée (systèmes externes CHU). Authentifiée par jeton d'API (pas de session JWT).
 * Protégée par limitation de débit (PareFeuFilter) et par le module « interop ».
 */
@RestController
@RequestMapping("/api/public")
public class InteropPublicController {

    private final InteropService interop;
    private final ModuleService modules;
    private final FormulaireRepository formulaires;
    private final SaisieService saisie;

    public InteropPublicController(InteropService interop, ModuleService modules,
                                   FormulaireRepository formulaires, SaisieService saisie) {
        this.interop = interop;
        this.modules = modules;
        this.formulaires = formulaires;
        this.saisie = saisie;
    }

    /** OAuth2 client_credentials (simplifié) : échange identifiants client → jeton d'accès. */
    @PostMapping("/oauth/token")
    public JetonEmis jeton(@RequestParam("client_id") String clientId, @RequestParam("client_secret") String secret) {
        return interop.emettreJeton(clientId, secret);
    }

    /** Crée (et soumet) une fiche pour un formulaire, au nom d'un client d'API. */
    @PostMapping("/v1/formulaires/{code}/fiches")
    public SaisieVue creerFiche(@PathVariable String code,
                                @RequestHeader(value = "Authorization", required = false) String authz,
                                @RequestBody(required = false) String donneesJson) {
        ContexteApi ctx = authentifier(authz);
        if (!modules.estActif(ctx.tenantId(), "interop"))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Module interop inactif.");
        try {
            TenantContext.set(ctx.tenantId());
            Long fid = formulaires.findByTenantIdAndCode(ctx.tenantId(), code)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Formulaire inconnu : " + code))
                    .getId();
            SaisieVue v = saisie.creer(fid, ctx.clientId());
            return saisie.soumettre(v.id(), new SoumissionRequest(donneesJson, null));
        } finally {
            TenantContext.clear();
        }
    }

    private ContexteApi authentifier(String authz) {
        String token = (authz != null && authz.startsWith("Bearer ")) ? authz.substring(7) : null;
        return interop.authentifier(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide ou expiré."));
    }
}
