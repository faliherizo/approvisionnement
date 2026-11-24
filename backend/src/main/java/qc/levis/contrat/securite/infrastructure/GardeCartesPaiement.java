package qc.levis.contrat.securite.infrastructure;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.SequenceInputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import qc.levis.contrat.securite.domain.NumeroCarte;

/**
 * Refuse à la frontière toute requête qui porte un numéro de carte de paiement (IQ-D-14, ADR-022) : aucun PAN n'est
 * stocké, traité ni transmis, la plateforme reste hors du périmètre PCI DSS. Le numéro n'est jamais journalisé.
 */
@Component
@Order(2)
public class GardeCartesPaiement extends OncePerRequestFilter {

    /** Au-delà, le reste du corps est transmis sans être inspecté (limite documentée dans l'ADR). */
    static final int TAILLE_INSPECTEE = 2 * 1024 * 1024;
    private static final Set<String> ECRITURES = Set.of("POST", "PUT", "PATCH");
    private static final Logger LOG = LoggerFactory.getLogger(GardeCartesPaiement.class);

    private final boolean actif;
    private final List<String> exemptes;

    public GardeCartesPaiement(@Value("${app.securite.cartes.actif:true}") boolean actif,
                               @Value("${app.securite.cartes.chemins-exemptes:}") List<String> exemptes) {
        this.actif = actif;
        this.exemptes = exemptes.stream().map(String::strip).filter(s -> !s.isEmpty()).toList();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest req) {
        String uri = req.getRequestURI();
        boolean vise = uri.startsWith("/api/") || uri.startsWith("/scim/");
        return !actif || !vise || exemptes.stream().anyMatch(uri::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        Optional<NumeroCarte.Reseau> trouve = NumeroCarte.detecter(decoder(req.getQueryString()));
        HttpServletRequest suite = req;
        boolean ecriture = ECRITURES.contains(req.getMethod());
        if (trouve.isEmpty() && ecriture && formulaire(req.getContentType())) {
            // Le conteneur lit lui-même un corps de formulaire : on inspecte ses paramètres, sans consommer le flux.
            trouve = NumeroCarte.detecter(String.join("\n", req.getParameterMap().values().stream().flatMap(Arrays::stream).toList()));
        } else if (trouve.isEmpty() && ecriture && textuel(req.getContentType())) {
            byte[] debut = req.getInputStream().readNBytes(TAILLE_INSPECTEE);
            trouve = NumeroCarte.detecter(new String(debut, StandardCharsets.UTF_8));
            suite = new CorpsRelu(req, debut);
        }
        if (trouve.isPresent()) {
            LOG.warn("Numéro de carte de paiement refusé ({}) : {} {}", trouve.get(), req.getMethod(), req.getRequestURI());
            refuser(res);
            return;
        }
        chain.doFilter(suite, res);
    }

    private static void refuser(HttpServletResponse res) throws IOException {
        res.setStatus(422);
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write("{\"error\":\"carte_paiement_refusee\",\"message\":\"Un numéro de carte de paiement a été "
                + "détecté. La plateforme n'accepte aucune donnée de carte : retirez le numéro, puis recommencez.\"}");
    }

    private static boolean textuel(String type) {
        if (type == null) {
            return false;
        }
        String t = type.toLowerCase(Locale.ROOT);
        return t.contains("json") || t.startsWith("text/") || t.contains("xml");
    }

    private static boolean formulaire(String type) {
        return type != null && type.toLowerCase(Locale.ROOT).startsWith("application/x-www-form-urlencoded");
    }

    private static String decoder(String texte) {
        if (texte == null) {
            return null;
        }
        try {
            return URLDecoder.decode(texte, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException malEncode) {
            return texte;
        }
    }

    /** Requête dont le début du corps, déjà lu pour l'inspection, est rendu avant le reste du flux d'origine. */
    static final class CorpsRelu extends HttpServletRequestWrapper {

        private final ServletInputStream flux;

        CorpsRelu(HttpServletRequest req, byte[] debut) throws IOException {
            super(req);
            SequenceInputStream suite = new SequenceInputStream(new ByteArrayInputStream(debut), req.getInputStream());
            this.flux = new ServletInputStream() {
                private boolean fini;

                @Override
                public int read() throws IOException {
                    int b = suite.read();
                    fini = b < 0;
                    return b;
                }

                @Override
                public int read(byte[] tampon, int debutTampon, int longueur) throws IOException {
                    int n = suite.read(tampon, debutTampon, longueur);
                    fini = n < 0;
                    return n;
                }

                @Override
                public boolean isFinished() {
                    return fini;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(ReadListener ecouteur) {
                    throw new UnsupportedOperationException("Lecture asynchrone non prise en charge.");
                }
            };
        }

        @Override
        public ServletInputStream getInputStream() {
            return flux;
        }

        @Override
        public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(flux, StandardCharsets.UTF_8));
        }
    }
}
