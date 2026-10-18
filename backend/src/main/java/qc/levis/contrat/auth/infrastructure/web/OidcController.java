package qc.levis.contrat.auth.infrastructure.web;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import qc.levis.contrat.auth.application.OidcSessionService;
import qc.levis.contrat.auth.domain.OidcFlowState.Etat;
import qc.levis.contrat.auth.domain.OidcFlowState;
import qc.levis.contrat.auth.infrastructure.JwtAuthenticationFilter;
import qc.levis.contrat.auth.infrastructure.OidcClient.Decouverte;
import qc.levis.contrat.auth.infrastructure.OidcClient.Identite;
import qc.levis.contrat.auth.infrastructure.OidcClient.OidcException;
import qc.levis.contrat.auth.infrastructure.OidcClient;
import qc.levis.contrat.integration.application.IntegrationOidcService.ConfigOidc;
import qc.levis.contrat.integration.application.IntegrationOidcService;
import qc.levis.contrat.tenant.application.repository.TenantRepository;
import qc.levis.contrat.tenant.domain.Tenant;

/**
 * Flux d'authentification OpenID Connect (authorization code + PKCE), points d'accès publics. Le
 * tenant est résolu par sous-domaine (slug) et sa configuration est chargée dynamiquement : aucune
 * inscription de client OAuth2 au démarrage. À la validation du jeton d'identité, une session locale
 * est émise (cookie httpOnly), comme pour la connexion par mot de passe.
 */
@RestController
@RequestMapping("/api/auth/oidc")
public class OidcController {

    private static final Logger log = LoggerFactory.getLogger(OidcController.class);

    /** Cookie httpOnly signé, de courte durée, portant l'état du flux (state/nonce/PKCE). */
    static final String FLOW_COOKIE = "OIDC_FLOW";
    static final String FLOW_PATH = "/api/auth/oidc";
    static final String CALLBACK_PATH = "/api/auth/oidc/callback";

    private final SecureRandom random = new SecureRandom();
    private final IntegrationOidcService service;
    private final OidcClient client;
    private final OidcFlowState flowState;
    private final OidcSessionService sessions;
    private final TenantRepository tenants;

    @Value("${app.jwt.expiration-ms}") private long expirationMs;
    @Value("${app.cookie.secure:false}") private boolean cookieSecure;
    @Value("${app.cookie.same-site:Lax}") private String sameSite;
    @Value("${app.oidc.redirect-base-url:}") private String redirectBaseConfig;

    public OidcController(IntegrationOidcService service, OidcClient client, OidcFlowState flowState,
                          OidcSessionService sessions, TenantRepository tenants) {
        this.service = service;
        this.client = client;
        this.flowState = flowState;
        this.sessions = sessions;
        this.tenants = tenants;
    }

    /** Démarre le flux : découverte, génération PKCE, dépôt du cookie de flux et redirection vers l'IdP. */
    @GetMapping("/login")
    public ResponseEntity<Void> login(@RequestParam(required = false) String slug,
                                      HttpServletRequest request) {
        Long tenantId = tenantParSlug(slug);
        ConfigOidc cfg = service.configClient(tenantId).orElse(null);
        if (cfg == null) return redirection("/login?oidc=indisponible", null);
        Decouverte d = client.decouvrir(cfg.issuer())
                .filter(x -> x.authorizationEndpoint() != null).orElse(null);
        if (d == null) return redirection("/login?oidc=erreur", null);

        String state = alea();
        String verifier = alea();
        String nonce = alea();
        String redirectUri = redirectBase(request) + CALLBACK_PATH;
        String flow = flowState.signer(new Etat(state, nonce, verifier, tenantId, redirectUri));
        URI autorisation = URI.create(construireUrlAutorisation(d, cfg, state, nonce, redirectUri, verifier));
        return ResponseEntity.status(302).location(autorisation)
                .header(HttpHeaders.SET_COOKIE, cookieFlux(flow, flowState.getTtlMs()).toString())
                .build();
    }

    /** Retour de l'IdP : vérifie le state, échange le code, valide le jeton et ouvre la session. */
    @GetMapping("/callback")
    public ResponseEntity<Void> callback(@RequestParam(required = false) String code,
                                         @RequestParam(required = false) String state,
                                         @RequestParam(required = false) String error,
                                         HttpServletRequest request) {
        if (error != null) return redirection("/login?oidc=refuse", cookieFlux("", 0));
        Etat etat = lireFlux(request).orElse(null);
        if (etat == null || code == null || !etat.state().equals(state)) {
            return redirection("/login?oidc=erreur", cookieFlux("", 0));
        }
        ConfigOidc cfg = service.configClient(etat.tenantId()).orElse(null);
        if (cfg == null) return redirection("/login?oidc=indisponible", cookieFlux("", 0));
        try {
            Decouverte d = client.decouvrir(cfg.issuer())
                    .orElseThrow(() -> new OidcException("Découverte OIDC impossible."));
            Identite id = client.echangerEtValider(d, cfg, code, etat.redirectUri(),
                    etat.codeVerifier(), etat.nonce());
            String token = sessions.etablirSession(cfg, id.courriel(), id.nom());
            return ResponseEntity.status(302).location(URI.create("/"))
                    .header(HttpHeaders.SET_COOKIE, cookieSession(token, expirationMs).toString())
                    .header(HttpHeaders.SET_COOKIE, cookieFlux("", 0).toString())
                    .build();
        } catch (OidcException e) {
            log.warn("Échec du flux OIDC (callback) : {}", e.getMessage());
            return redirection("/login?oidc=erreur", cookieFlux("", 0));
        }
    }

    private Long tenantParSlug(String slug) {
        if (slug == null || slug.isBlank()) return null;
        return tenants.findBySlug(slug).map(Tenant::getId).orElse(null);
    }

    private String construireUrlAutorisation(Decouverte d, ConfigOidc cfg, String state, String nonce,
                                             String redirectUri, String verifier) {
        String sep = d.authorizationEndpoint().contains("?") ? "&" : "?";
        return d.authorizationEndpoint() + sep
                + "response_type=code"
                + "&client_id=" + enc(cfg.clientId())
                + "&redirect_uri=" + enc(redirectUri)
                + "&scope=" + enc(cfg.scopes() == null ? "openid profile email" : cfg.scopes())
                + "&state=" + enc(state)
                + "&nonce=" + enc(nonce)
                + "&code_challenge=" + enc(challenge(verifier))
                + "&code_challenge_method=S256";
    }

    private Optional<Etat> lireFlux(HttpServletRequest request) {
        if (request.getCookies() == null) return Optional.empty();
        for (Cookie c : request.getCookies()) {
            if (FLOW_COOKIE.equals(c.getName())) return flowState.lire(c.getValue());
        }
        return Optional.empty();
    }

    private ResponseEntity<Void> redirection(String chemin, ResponseCookie cookie) {
        var builder = ResponseEntity.status(302).location(URI.create(chemin));
        if (cookie != null) builder.header(HttpHeaders.SET_COOKIE, cookie.toString());
        return builder.build();
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

    /** Cookie de flux httpOnly, restreint aux points d'accès OIDC, SameSite=Lax (retour de l'IdP). */
    private ResponseCookie cookieFlux(String valeur, long maxAgeMs) {
        return ResponseCookie.from(FLOW_COOKIE, valeur)
                .httpOnly(true).secure(cookieSecure).sameSite("Lax").path(FLOW_PATH)
                .maxAge(Duration.ofMillis(maxAgeMs)).build();
    }

    private String alea() {
        byte[] buf = new byte[32];
        random.nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }

    private String challenge(String verifier) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(verifier.getBytes(StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 indisponible", e);
        }
    }

    private String enc(String v) {
        return URLEncoder.encode(v == null ? "" : v, StandardCharsets.UTF_8);
    }
}
