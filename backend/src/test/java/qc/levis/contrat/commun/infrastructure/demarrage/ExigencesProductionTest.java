package qc.levis.contrat.commun.infrastructure.demarrage;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

/** OWASP A05 / A02 : la production refuse de démarrer avec un secret ou un réglage de développement. */
class ExigencesProductionTest {

    static MockEnvironment conforme() {
        return new MockEnvironment().withProperty("app.jwt.secret", "x".repeat(64)).withProperty("app.crypto.cle", "cle-de-production-robuste")
                .withProperty("app.crypto.sel", "a1b2c3d4e5f60718").withProperty("spring.datasource.password", "mdp-fort")
                .withProperty("app.cookie.secure", "true");
    }

    @Test
    void une_configuration_de_production_conforme_demarre() {
        assertEquals(List.of(), ExigencesProduction.manquements(conforme()));
        assertDoesNotThrow(() -> new ExigencesProduction(conforme()).verifier());
    }

    @Test
    void chaque_reglage_de_developpement_est_signale_et_bloque_le_demarrage() {
        MockEnvironment env = new MockEnvironment().withProperty("app.jwt.secret", ExigencesProduction.JWT_DEFAUT)
                .withProperty("app.crypto.cle", ExigencesProduction.CLE_DEFAUT).withProperty("app.crypto.sel", ExigencesProduction.SEL_DEFAUT)
                .withProperty("spring.datasource.password", "contrat").withProperty("app.cookie.secure", "false");
        assertEquals(5, ExigencesProduction.manquements(env).size());
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> new ExigencesProduction(env).verifier());
        assertTrue(e.getMessage().contains("JWT_SECRET"));
        assertEquals(List.of("JWT_SECRET absent, par défaut ou de moins de 256 bits"),
                ExigencesProduction.manquements(conforme().withProperty("app.jwt.secret", "court")));
        assertEquals(List.of("APP_CRYPTO_KEY absente ou par défaut"), ExigencesProduction.manquements(conforme().withProperty("app.crypto.cle", "court")));
    }

    @Test
    void la_demonstration_en_http_local_ne_deroge_qu_au_cookie_secure() {
        MockEnvironment env = conforme().withProperty("app.cookie.secure", "false").withProperty("app.securite.http-local-autorise", "true");
        assertEquals(List.of(), ExigencesProduction.manquements(env));
    }
}
