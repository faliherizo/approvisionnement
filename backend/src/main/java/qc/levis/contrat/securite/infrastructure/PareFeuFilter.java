package qc.levis.contrat.securite.infrastructure;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import qc.levis.contrat.securite.application.LimiteurDebit;

/**
 * Pare-feu applicatif : limite le débit par IP sur l'API publique (anti-abus / anti-force brute).
 * Renvoie 429 au-delà du seuil. (Le WAF OWASP complet est fourni par l'edge cloud ; ceci est la
 * protection applicative de base, portée de la conception {@code securite/pareFeu.ts}.)
 */
@Component
@Order(1)
public class PareFeuFilter extends OncePerRequestFilter {

    private static final int MAX_PAR_MINUTE = 120;
    private static final long FENETRE_MS = 60_000L;
    private final LimiteurDebit limiteur = new LimiteurDebit();

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String uri = req.getRequestURI();
        if (uri != null && uri.startsWith("/api/public/")) {
            String cle = clientIp(req);
            if (!limiteur.autoriser(cle, MAX_PAR_MINUTE, FENETRE_MS, System.currentTimeMillis())) {
                res.setStatus(429);
                res.setContentType("application/json");
                res.getWriter().write("{\"erreur\":\"Trop de requêtes. Réessayez plus tard.\"}");
                return;
            }
        }
        chain.doFilter(req, res);
    }

    private String clientIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) return xff.split(",")[0].trim();
        return req.getRemoteAddr();
    }
}
