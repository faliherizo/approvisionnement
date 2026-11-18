package qc.levis.contrat.auth.infrastructure.web;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import qc.levis.contrat.auth.application.SamlSessionService;
import qc.levis.contrat.auth.domain.SamlFlowState;
import qc.levis.contrat.auth.infrastructure.JwtAuthenticationFilter;
import qc.levis.contrat.auth.infrastructure.SamlClient.Identite;
import qc.levis.contrat.auth.infrastructure.SamlClient.SamlException;
import qc.levis.contrat.auth.infrastructure.SamlClient;
import qc.levis.contrat.integration.application.IntegrationSamlService.ConfigSaml;
import qc.levis.contrat.integration.application.IntegrationSamlService;
import qc.levis.contrat.tenant.application.repository.TenantRepository;
import qc.levis.contrat.tenant.domain.Tenant;

/**
 * Flux d'authentification SAML 2.0 SP-initiated, points d'accès publics. Le tenant est résolu par
 * sous-domaine (slug) et sa configuration chargée dynamiquement : aucune inscription de Relying
 * Party au démarrage. À la validation de l'assertion signée reçue à l'ACS, une session locale est
 * émise (cookie httpOnly), comme pour la connexion par mot de passe.
 */
@RestController
@RequestMapping("/api/auth/saml")
public class SamlController {

    private static final Logger log = LoggerFactory.getLogger(SamlController.class);

    static final String CALLBACK_PATH = "/api/auth/saml/callback";

    private final IntegrationSamlService service;
    private final SamlClient client;
    private final SamlFlowState flowState;
    private final SamlSessionService sessions;
    private final TenantRepository tenants;

    @Value("${app.jwt.expiration-ms}") private long expirationMs;
    @Value("${app.cookie.secure:false}") private boolean cookieSecure;
    @Value("${app.cookie.same-site:Lax}") private String sameSite;
    @Value("${app.saml.redirect-base-url:${app.oidc.redirect-base-url:}}") private String redirectBaseConfig;

    public SamlController(IntegrationSamlService service, SamlClient client, SamlFlowState flowState,
                          SamlSessionService sessions, TenantRepository tenants) {
        this.service = service;
        this.client = client;
        this.flowState = flowState;
        this.sessions = sessions;
        this.tenants = tenants;
    }

    /** Démarre le flux : construit l'AuthnRequest et redirige (binding HTTP-Redirect) vers le SSO de l'IdP. */
    @GetMapping("/login")
    public ResponseEntity<Void> login(@RequestParam(required = false) String slug,
                                      HttpServletRequest request) {
        Long tenantId = tenantParSlug(slug);
        ConfigSaml cfg = service.configClient(tenantId).orElse(null);
        if (cfg == null) return redirection("/login?saml=indisponible");
        String acsUrl = redirectBase(request) + CALLBACK_PATH;
        String relayState = flowState.signer(tenantId);
        URI destination = URI.create(client.urlRedirection(cfg, acsUrl, relayState));
        return ResponseEntity.status(302).location(destination).build();
    }

    /** ACS : reçoit la SAMLResponse (POST), valide l'assertion signée et ouvre la session. */
    @PostMapping("/callback")
    public ResponseEntity<Void> callback(@RequestParam(name = "SAMLResponse", required = false) String samlResponse,
                                         @RequestParam(name = "RelayState", required = false) String relayState) {
        Long tenantId = flowState.lireTenant(relayState).orElse(null);
        if (tenantId == null || samlResponse == null) return redirection("/login?saml=erreur");
        ConfigSaml cfg = service.configClient(tenantId).orElse(null);
        if (cfg == null) return redirection("/login?saml=indisponible");
        try {
            Identite id = client.validerReponse(cfg, samlResponse);
            String token = sessions.etablirSession(cfg, id.courriel(), id.nom(), id.faits());
            return ResponseEntity.status(302).location(URI.create("/"))
                    .header(HttpHeaders.SET_COOKIE, cookieSession(token, expirationMs).toString())
                    .build();
        } catch (SamlException e) {
            log.warn("Échec du flux SAML (callback) : {}", e.getMessage());
            return redirection("/login?saml=erreur");
        }
    }

    private Long tenantParSlug(String slug) {
        if (slug == null || slug.isBlank()) return null;
        return tenants.findBySlug(slug).map(Tenant::getId).orElse(null);
    }

    private ResponseEntity<Void> redirection(String chemin) {
        return ResponseEntity.status(302).location(URI.create(chemin)).build();
    }

    private String redirectBase(HttpServletRequest request) {
        if (redirectBaseConfig != null && !redirectBaseConfig.isBlank()) {
            return redirectBaseConfig.endsWith("/")
                    ? redirectBaseConfig.substring(0, redirectBaseConfig.length() - 1) : redirectBaseConfig;
        }
        int port = request.getServerPort();
        boolean portParDefaut = ("http".equals(request.getScheme()) && port == 80)
                || ("https".equals(request.getScheme()) && port == 443);
        return request.getScheme() + "://" + request.getServerName()
                + (portParDefaut ? "" : ":" + port);
    }

    /** Cookie de session httpOnly (identique à la connexion par mot de passe). */
    private ResponseCookie cookieSession(String valeur, long maxAgeMs) {
        return ResponseCookie.from(JwtAuthenticationFilter.COOKIE_NAME, valeur)
                .httpOnly(true).secure(cookieSecure).sameSite(sameSite).path("/")
                .maxAge(Duration.ofMillis(maxAgeMs)).build();
    }
}
