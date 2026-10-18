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

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
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
        if (token != null) {
            try {
                Claims claims = jwtService.parser(token);
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
