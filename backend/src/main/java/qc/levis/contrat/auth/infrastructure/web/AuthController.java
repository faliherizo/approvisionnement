package qc.levis.contrat.auth.infrastructure.web;

import jakarta.validation.Valid;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.bind.annotation.*;
import qc.levis.contrat.auth.application.AuthService;
import qc.levis.contrat.auth.application.TotpService;
import qc.levis.contrat.auth.application.dto.AuthResponse;
import qc.levis.contrat.auth.application.dto.LoginRequest;
import qc.levis.contrat.auth.application.dto.MeResponse;
import qc.levis.contrat.auth.application.dto.QuestionItem;
import qc.levis.contrat.auth.application.dto.QuestionSetupRequest;
import qc.levis.contrat.auth.application.dto.TotpEnableRequest;
import qc.levis.contrat.auth.application.dto.TotpSetupResponse;
import qc.levis.contrat.auth.application.repository.UserRepository;
import qc.levis.contrat.auth.domain.SecurityQuestion;
import qc.levis.contrat.auth.domain.User;
import qc.levis.contrat.auth.infrastructure.JwtAuthenticationFilter;
import qc.levis.contrat.commun.domain.Enums;
import qc.levis.contrat.integration.application.IntegrationOidcService;
import qc.levis.contrat.integration.application.IntegrationSamlService;
import qc.levis.contrat.integration.domain.IntegrationOidc;
import qc.levis.contrat.integration.domain.IntegrationSaml;
import qc.levis.contrat.tenant.application.repository.TenantRepository;
import qc.levis.contrat.tenant.domain.Tenant;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final qc.levis.contrat.auth.application.LimiteurConnexions limiteur;
    private final qc.levis.contrat.auth.application.RevocationJetons revocation;
    private final AuthService auth;
    private final UserRepository users;
    private final TotpService totp;
    private final TenantRepository tenants;
    private final IntegrationOidcService oidc;
    private final IntegrationSamlService saml;
    /** Optionnel : présent uniquement si un client OAuth2 (ex. profil « azure ») est configuré. */
    private final ObjectProvider<ClientRegistrationRepository> clientRegistrations;

    @Value("${app.jwt.expiration-ms}") private long expirationMs;
    @Value("${app.cookie.secure:false}") private boolean cookieSecure;
    @Value("${app.cookie.same-site:Lax}") private String sameSite;

    public AuthController(AuthService auth, UserRepository users, TotpService totp,
                          TenantRepository tenants, IntegrationOidcService oidc,
                          IntegrationSamlService saml,
                          ObjectProvider<ClientRegistrationRepository> clientRegistrations,
                          qc.levis.contrat.auth.application.LimiteurConnexions limiteur,
                          qc.levis.contrat.auth.application.RevocationJetons revocation) {
        this.limiteur = limiteur;
        this.revocation = revocation;
        this.auth = auth;
        this.users = users;
        this.totp = totp;
        this.tenants = tenants;
        this.oidc = oidc;
        this.saml = saml;
        this.clientRegistrations = clientRegistrations;
    }

    /**
     * Vérifie, en accès multi-tenant par sous-domaine, que l'utilisateur appartient bien au
     * tenant du sous-domaine ({@code slug}). Le superadmin (tenantId nul) est autorisé partout ;
     * un slug absent/inconnu désactive le contrôle (rétrocompatible dev / domaine nu).
     */
    private boolean tenantIncompatible(User u, String slug) {
        if (slug == null || slug.isBlank() || u.getTenantId() == null) {
            return false;
        }
        return tenants.findBySlug(slug)
                .map(Tenant::getId)
                .map(id -> !id.equals(u.getTenantId()))
                .orElse(false);
    }

    /**
     * Configuration publique de la page de connexion. {@code oidcActif} n'est vrai que si le tenant
     * (résolu par sous-domaine {@code slug}) a une connexion OIDC active et complète ; {@code oidcLibelle}
     * fournit alors le texte du bouton. {@code ssoAzure} reste la bascule héritée (profil « azure »).
     */
    @GetMapping("/config")
    public Map<String, Object> config(@RequestParam(required = false) String slug) {
        Long tenantId = tenantIdParSlug(slug);
        IntegrationOidc c = oidc.pour(tenantId);
        IntegrationSaml s = saml.pour(tenantId);
        Map<String, Object> config = new HashMap<>();
        config.put("ssoAzure", ssoAzureActif());
        config.put("oidcActif", c.isActif() && c.estConfigure());
        config.put("oidcLibelle", c.getBoutonLibelle());
        config.put("samlActif", s.isActif() && s.estConfigure());
        config.put("samlLibelle", s.getBoutonLibelle());
        return config;
    }

    private Long tenantIdParSlug(String slug) {
        if (slug == null || slug.isBlank()) return null;
        return tenants.findBySlug(slug).map(Tenant::getId).orElse(null);
    }

    private boolean ssoAzureActif() {
        ClientRegistrationRepository repo = clientRegistrations.getIfAvailable();
        if (repo == null) return false;
        try {
            return repo.findByRegistrationId("azure") != null;
        } catch (RuntimeException e) {
            return false;
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        if (limiteur.estSuspendu(req.email())) {
            return ResponseEntity.status(429).header(HttpHeaders.RETRY_AFTER, String.valueOf(limiteur.fenetreMinutes() * 60))
                    .body(AuthResponse.echec("Trop de tentatives de connexion : réessayez dans " + limiteur.fenetreMinutes() + " minutes."));
        }
        AuthService.Outcome r = auth.authentifier(
                req.email(), req.motDePasse(), req.totpCode(), req.securityAnswers());

        return switch (r.status()) {
            case SUCCES -> {
                if (tenantIncompatible(r.user(), req.slug())) {
                    yield ResponseEntity.status(401).body(AuthResponse.echec(
                            "Ce compte n'appartient pas à l'établissement « " + req.slug() + " »."));
                }
                List<String> roles = r.user().getRoles().stream().map(Enum::name).toList();
                yield ResponseEntity.ok()
                        .header(HttpHeaders.SET_COOKIE, cookieSession(r.token(), expirationMs).toString())
                        .body(AuthResponse.succes(r.user().getEmail(), r.user().getNom(), roles));
            }
            case DEFI_MFA -> ResponseEntity.ok(AuthResponse.defiMfa(r.methode(), r.questions()));
            case ECHEC -> ResponseEntity.status(401).body(AuthResponse.echec(r.erreur()));
        };
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(Authentication authentication, jakarta.servlet.http.HttpServletRequest requete) {
        jakarta.servlet.http.Cookie session = org.springframework.web.util.WebUtils.getCookie(requete, JwtAuthenticationFilter.COOKIE_NAME);
        if (session != null) revocation.revoquer(session.getValue());
        if (authentication != null) {
            auth.journaliserDeconnexion(authentication.getName());
        }
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookieSession("", 0).toString())
                .build();
    }

    @GetMapping("/me")
    public ResponseEntity<MeResponse> me(Authentication authentication) {
        if (authentication == null) return ResponseEntity.status(401).build();
        User u = users.findByEmail(authentication.getName()).orElse(null);
        if (u == null) return ResponseEntity.status(401).build();
        List<String> roles = u.getRoles().stream().map(Enum::name).toList();
        return ResponseEntity.ok(new MeResponse(
                u.getEmail(), u.getNom(), u.getDirection(), roles,
                u.isMfaActive(), u.getMfaMethode() != null ? u.getMfaMethode().name() : "NONE"));
    }

    // --- Configuration du second facteur (utilisateur authentifié) ---

    @PostMapping("/mfa/totp/setup")
    public TotpSetupResponse setupTotp(Authentication authentication) {
        User u = utilisateurCourant(authentication);
        String secret = totp.genererSecret();
        u.setTotpSecret(secret);
        users.save(u);
        return new TotpSetupResponse(secret, totp.uriOtpauth(secret, u.getEmail(), "Ville de Levis"));
    }

    @PostMapping("/mfa/totp/enable")
    public ResponseEntity<AuthResponse> enableTotp(@Valid @RequestBody TotpEnableRequest req,
                                                        Authentication authentication) {
        User u = utilisateurCourant(authentication);
        if (u.getTotpSecret() == null || !totp.verifier(u.getTotpSecret(), req.code())) {
            return ResponseEntity.status(400).body(AuthResponse.echec("Code TOTP invalide"));
        }
        u.setMfaActive(true);
        u.setMfaMethode(Enums.MfaMethode.TOTP);
        users.save(u);
        return ResponseEntity.ok(AuthResponse.succes(u.getEmail(), u.getNom(),
                u.getRoles().stream().map(Enum::name).toList()));
    }

    @PostMapping("/mfa/question/setup")
    public ResponseEntity<Void> setupQuestions(@Valid @RequestBody QuestionSetupRequest req,
                                               Authentication authentication) {
        User u = utilisateurCourant(authentication);
        u.getQuestionsSecurite().clear();
        for (QuestionItem item : req.questions()) {
            u.getQuestionsSecurite().add(new SecurityQuestion(item.question(), auth.hacher(item.reponse())));
        }
        u.setMfaActive(true);
        u.setMfaMethode(Enums.MfaMethode.QUESTION);
        users.save(u);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/mfa/disable")
    public ResponseEntity<Void> disableMfa(Authentication authentication) {
        User u = utilisateurCourant(authentication);
        u.setMfaActive(false);
        u.setMfaMethode(Enums.MfaMethode.NONE);
        users.save(u);
        return ResponseEntity.noContent().build();
    }

    private User utilisateurCourant(Authentication authentication) {
        return users.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Utilisateur non authentifié"));
    }

    /** Cookie de session httpOnly (SameSite, Secure paramétrables). */
    private ResponseCookie cookieSession(String valeur, long maxAgeMs) {
        return ResponseCookie.from(JwtAuthenticationFilter.COOKIE_NAME, valeur)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(sameSite)
                .path("/")
                .maxAge(Duration.ofMillis(maxAgeMs))
                .build();
    }
}
