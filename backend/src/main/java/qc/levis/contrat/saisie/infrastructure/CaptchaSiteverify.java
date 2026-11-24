package qc.levis.contrat.saisie.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import qc.levis.contrat.saisie.application.port.CaptchaPublic;
import qc.levis.contrat.saisie.application.port.CaptchaVerifier;

/**
 * Captcha réel par le protocole « siteverify » commun à Cloudflare Turnstile et hCaptcha (CHU 3.07, ADR-026) :
 * {@code POST secret=…&response=…} (formulaire encodé), réponse JSON {@code {"success": true|false, …}}. La clé secrète
 * vient de l'environnement seulement (CAPTCHA_SECRET). Fermé par défaut : réponse illisible, refus ou panne du
 * fournisseur valent un échec, journalisé sans le jeton.
 */
@Component
@ConditionalOnProperty(name = "app.saisie.captcha.mode", havingValue = "siteverify")
public class CaptchaSiteverify implements CaptchaVerifier {

    private static final Logger log = LoggerFactory.getLogger(CaptchaSiteverify.class);
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Set<String> FOURNISSEURS = Set.of("TURNSTILE", "HCAPTCHA");
    /** Adresses publiées par chaque fournisseur, retenues quand la configuration n'en impose pas une autre. */
    private static final Map<String, String> URLS = Map.of(
            "TURNSTILE", "https://challenges.cloudflare.com/turnstile/v0/siteverify",
            "HCAPTCHA", "https://api.hcaptcha.com/siteverify");

    private final CaptchaPublic publique;
    private final URI url;
    private final String secret;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    public CaptchaSiteverify(@Value("${app.saisie.captcha.fournisseur:TURNSTILE}") String fournisseur,
                             @Value("${app.saisie.captcha.cle-site:}") String cleSite,
                             @Value("${app.saisie.captcha.secret:}") String secret,
                             @Value("${app.saisie.captcha.url:}") String url) {
        String f = fournisseur == null ? "" : fournisseur.strip().toUpperCase(Locale.ROOT);
        if (!FOURNISSEURS.contains(f)) {
            throw new IllegalStateException("Captcha : fournisseur « " + fournisseur + " » inconnu (TURNSTILE ou HCAPTCHA).");
        }
        if (blanc(cleSite) || blanc(secret)) {
            throw new IllegalStateException("Captcha : CAPTCHA_CLE_SITE et CAPTCHA_SECRET sont requis.");
        }
        this.publique = new CaptchaPublic(f, cleSite.strip());
        this.secret = secret.strip();
        this.url = URI.create(blanc(url) ? URLS.get(f) : url.strip());
    }

    @Override
    public boolean verifier(String token) {
        if (blanc(token)) {
            return false;
        }
        String corps = "secret=" + encoder(secret) + "&response=" + encoder(token)
                + "&sitekey=" + encoder(publique.cleSite());
        HttpRequest requete = HttpRequest.newBuilder(url).timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(corps)).build();
        try {
            HttpResponse<String> r = http.send(requete, HttpResponse.BodyHandlers.ofString());
            return r.statusCode() == 200 && reussi(r.body());
        } catch (IOException e) {
            log.warn("Captcha {} injoignable : défi refusé ({})", publique.fournisseur(), e.getClass().getSimpleName());
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    @Override
    public CaptchaPublic configurationPublique() {
        return publique;
    }

    static boolean reussi(String corps) {
        try {
            var n = JSON.readTree(corps);
            if (n.path("success").asBoolean(false)) {
                return true;
            }
            log.info("Défi anti-robot refusé : {}", n.path("error-codes"));
            return false;
        } catch (IOException | RuntimeException illisible) {
            log.warn("Captcha : réponse illisible, défi refusé");
            return false;
        }
    }

    private static String encoder(String v) {
        return URLEncoder.encode(v, StandardCharsets.UTF_8);
    }

    private static boolean blanc(String s) {
        return s == null || s.isBlank();
    }
}
