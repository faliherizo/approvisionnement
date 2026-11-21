package qc.levis.contrat.commun.infrastructure.demarrage;

import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Refus de démarrer en production avec un réglage de développement (OWASP A05, A02). Les valeurs par défaut des fichiers
 * de configuration servent au poste de développement ; en production, un secret par défaut permettrait de forger des
 * jetons de session (secret JWT connu) ou de déchiffrer les secrets des organisations (clé connue).
 */
@Component
@Profile("prod")
public class ExigencesProduction {

    static final String JWT_DEFAUT = "changez-moi-cette-cle-secrete-de-256-bits-minimum-pour-la-ville-de-levis";
    static final String CLE_DEFAUT = "changez-moi-cle-de-chiffrement-des-secrets";
    static final String SEL_DEFAUT = "5c0ff33a1b2c3d4e";
    static final String MOT_DE_PASSE_BD_DEFAUT = "contrat";

    private final Environment env;

    public ExigencesProduction(Environment env) {
        this.env = env;
    }

    @PostConstruct
    void verifier() {
        List<String> manquements = manquements(env);
        if (!manquements.isEmpty()) {
            throw new IllegalStateException("Démarrage refusé en production : " + String.join(" ; ", manquements));
        }
    }

    static List<String> manquements(Environment env) {
        List<String> m = new ArrayList<>();
        String jwt = env.getProperty("app.jwt.secret", "");
        if (jwt.equals(JWT_DEFAUT) || jwt.getBytes(StandardCharsets.UTF_8).length < 32) m.add("JWT_SECRET absent, par défaut ou de moins de 256 bits");
        if (CLE_DEFAUT.equals(env.getProperty("app.crypto.cle")) || env.getProperty("app.crypto.cle", "").length() < 16) m.add("APP_CRYPTO_KEY absente ou par défaut");
        if (SEL_DEFAUT.equals(env.getProperty("app.crypto.sel"))) m.add("APP_CRYPTO_SALT par défaut");
        if (MOT_DE_PASSE_BD_DEFAUT.equals(env.getProperty("spring.datasource.password"))) m.add("DB_PASSWORD par défaut");
        boolean httpLocal = env.getProperty("app.securite.http-local-autorise", Boolean.class, false);
        if (!httpLocal && !env.getProperty("app.cookie.secure", Boolean.class, false)) m.add("COOKIE_SECURE doit valoir true (HTTPS)");
        return m;
    }
}
