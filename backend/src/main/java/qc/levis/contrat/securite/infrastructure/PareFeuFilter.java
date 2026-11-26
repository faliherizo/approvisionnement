package qc.levis.contrat.securite.infrastructure;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.LongSupplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import qc.levis.contrat.securite.application.LimiteurDebit;
import qc.levis.contrat.securite.domain.AdresseClient;

/**
 * Pare-feu applicatif : limite le débit, par adresse du client et par famille de routes, sur TOUTE la surface accessible
 * sans session (constat SEC-33) : connexion, authentification unique, portails à jeton, signature, intégration, SCIM, API
 * publique, pages publiques. Une famille saturée répond 429 sans gêner les autres. L'adresse est celle qu'ajoute le
 * mandataire de confiance (voir {@link AdresseClient}) : un X-Forwarded-For forgé ne contourne plus la limite. Le pare-feu
 * applicatif complet (WAF) reste celui du répartiteur ; ceci est la protection de base, présente partout.
 */
@Component
@Order(1)
public class PareFeuFilter extends OncePerRequestFilter {

    static final long FENETRE_MS = 60_000L;

    private final LimiteurDebit limiteur = new LimiteurDebit();
    private final int maxParMinute;
    private final List<String> familles;
    private final LongSupplier horloge;

    @Autowired
    public PareFeuFilter(@Value("${app.securite.pare-feu.max-par-minute:300}") int maxParMinute,
                         @Value("${app.securite.pare-feu.familles}") List<String> familles) {
        this(maxParMinute, familles, System::currentTimeMillis);
    }

    PareFeuFilter(int maxParMinute, List<String> familles, LongSupplier horloge) {
        this.maxParMinute = maxParMinute;
        this.familles = familles.stream().map(String::strip).filter(f -> !f.isEmpty()).toList();
        this.horloge = horloge;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        Optional<String> famille = famille(req.getRequestURI());
        if (famille.isEmpty()) {
            chain.doFilter(req, res);
            return;
        }
        String cle = AdresseClient.resoudre(req.getRemoteAddr(), req.getHeader("X-Forwarded-For")) + " " + famille.get();
        boolean autorise = limiteur.autoriser(cle, maxParMinute, FENETRE_MS, horloge.getAsLong());
        res.setHeader("RateLimit-Limit", String.valueOf(maxParMinute));
        if (!autorise) {
            res.setStatus(429);
            res.setHeader("Retry-After", String.valueOf(FENETRE_MS / 1000));
            res.setContentType("application/json");
            res.getWriter().write("{\"erreur\":\"Trop de requêtes. Réessayez plus tard.\"}");
            return;
        }
        chain.doFilter(req, res);
    }

    private Optional<String> famille(String uri) {
        if (uri == null) return Optional.empty();
        return familles.stream().filter(uri::startsWith).findFirst();
    }
}
