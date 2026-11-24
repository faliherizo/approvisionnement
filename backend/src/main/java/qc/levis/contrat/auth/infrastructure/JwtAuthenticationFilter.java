package qc.levis.contrat.auth.infrastructure;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import qc.levis.contrat.auth.application.JwtService;
import qc.levis.contrat.commun.infrastructure.TenantContext;

/** Filtre extrayant l'utilisateur et ses rôles depuis le jeton JWT. */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /** Nom du cookie httpOnly portant le jeton de session. */
    public static final String COOKIE_NAME = "ACCESS_TOKEN";

    private final JwtService jwtService;

    private final qc.levis.contrat.auth.application.RevocationJetons revocation;
    private final qc.levis.contrat.auth.application.PolitiqueSessionService politiqueSession;
    private final CookiesSession cookies;
    private final java.time.Clock horloge;

    /** Une session active n'est réémise qu'au plus une fois par minute : la limite d'inactivité reste exacte à la minute. */
    static final java.time.Duration RENOUVELLEMENT = java.time.Duration.ofMinutes(1);
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    public JwtAuthenticationFilter(JwtService jwtService, qc.levis.contrat.auth.application.RevocationJetons revocation,
                                   qc.levis.contrat.auth.application.PolitiqueSessionService politiqueSession,
                                   CookiesSession cookies, java.time.Clock horloge) {
        this.revocation = revocation;
        this.jwtService = jwtService;
        this.politiqueSession = politiqueSession;
        this.cookies = cookies;
        this.horloge = horloge;
    }

    /** Jeton porté par le cookie de session (navigateur) : seul celui-là se renouvelle par la réponse. */
    private static boolean parCookie(HttpServletRequest request, String token) {
        if (request.getCookies() == null) {
            return false;
        }
        for (Cookie c : request.getCookies()) {
            if (COOKIE_NAME.equals(c.getName()) && token.equals(c.getValue())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Politique de session (ST-01) : refuse une session inactive ou au-delà de sa durée maximale ; sinon, renouvelle le
     * cookie d'une session active, sans jamais dépasser la durée maximale. Rend vrai si la session est valide.
     */
    private boolean appliquerPolitique(Claims claims, Long tenant, String token, HttpServletRequest request, HttpServletResponse response) {
        java.time.Instant maintenant = horloge.instant();
        java.time.Instant emis = claims.getIssuedAt().toInstant();
        Number debutBrut = claims.get(JwtService.DEBUT_SESSION, Number.class);
        java.time.Instant debut = debutBrut == null ? emis : java.time.Instant.ofEpochSecond(debutBrut.longValue());
        qc.levis.contrat.auth.domain.LimitesSession limites = politiqueSession.limites(tenant);
        qc.levis.contrat.auth.domain.LimitesSession.Verdict verdict = limites.verifier(debut, emis, maintenant);
        if (verdict != qc.levis.contrat.auth.domain.LimitesSession.Verdict.VALIDE) {
            LOG.info("Session terminée ({}) pour l'organisation {}", verdict, tenant);
            return false;
        }
        if (parCookie(request, token) && !maintenant.isBefore(emis.plus(RENOUVELLEMENT))) {
            java.time.Duration validite = limites.validiteRenouvelee(debut, maintenant);
            @SuppressWarnings("unchecked")
            List<String> roles = claims.get("roles", List.class);
            String renouvele = jwtService.genererToken(claims.getSubject(), roles == null ? List.of() : roles, tenant, debut, validite);
            response.addHeader(org.springframework.http.HttpHeaders.SET_COOKIE, cookies.session(renouvele, validite).toString());
        }
        return true;
    }

    /** Jeton depuis le cookie httpOnly (SPA) ou l'en-tête Bearer (API/tests). */
    private String extraireToken(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if (COOKIE_NAME.equals(c.getName()) && c.getValue() != null && !c.getValue().isBlank()) {
                    return c.getValue();
                }
            }
        }
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain)
            throws ServletException, IOException {

        String token = extraireToken(request);
        if (token != null && !revocation.estRevoque(token)) {
            try {
                Claims claims = jwtService.parser(token);
                Number tenantRevendique = claims.get("tenant", Number.class);
                Long tenantJeton = tenantRevendique != null ? tenantRevendique.longValue() : null;
                if (!appliquerPolitique(claims, tenantJeton, token, request, response)) {
                    throw new IllegalStateException("Session terminée.");
                }
                String email = claims.getSubject();
                @SuppressWarnings("unchecked")
                List<String> roles = claims.get("roles", List.class);
                var authorities = (roles == null ? List.<String>of() : roles).stream()
                        .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                        .toList();
                var auth = new UsernamePasswordAuthenticationToken(email, null, authorities);
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);

                // Alimente le contexte de tenant (null = superadmin global).
                Number tenant = claims.get("tenant", Number.class);
                TenantContext.set(tenant != null ? tenant.longValue() : null);
            } catch (Exception e) {
                SecurityContextHolder.clearContext();
            }
        }
        try {
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear(); // évite toute fuite entre requêtes (pool de threads)
        }
    }
}
