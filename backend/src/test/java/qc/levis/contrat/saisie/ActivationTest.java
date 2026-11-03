package qc.levis.contrat.saisie;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.saisie.application.Activation;

/** Tests purs des fenêtres d'activation. */
class ActivationTest {

    private final Instant now = Instant.parse("2026-06-15T12:00:00Z");

    @Test
    void ouvert_sans_config() {
        assertTrue(Activation.raisonFermeture(null, now, 0).isEmpty());
    }

    @Test
    void pas_encore_ouvert() {
        var cfg = new Activation.Config(now.plus(1, ChronoUnit.DAYS), null, null, false, null);
        assertTrue(Activation.raisonFermeture(cfg, now, 0).orElse("").contains("pas encore"));
    }

    @Test
    void ferme_par_date() {
        var cfg = new Activation.Config(null, now.minus(1, ChronoUnit.DAYS), null, null, null);
        assertTrue(Activation.raisonFermeture(cfg, now, 0).orElse("").contains("fermé"));
    }

    @Test
    void capacite_atteinte() {
        var cfg = new Activation.Config(null, null, 100, null, null);
        assertTrue(Activation.raisonFermeture(cfg, now, 100).orElse("").contains("capacité"));
        assertTrue(Activation.raisonFermeture(cfg, now, 99).isEmpty());
    }

    @Test
    void captcha_requis() {
        assertTrue(Activation.captchaRequis(new Activation.Config(null, null, null, true, null)));
        assertFalse(Activation.captchaRequis(new Activation.Config(null, null, null, false, null)));
        assertFalse(Activation.captchaRequis(null));
    }

    @Test
    void plage_reseau() {
        assertTrue(Activation.ipAutorisee(null, "1.2.3.4"));                                   // pas de config
        var sansPlage = new Activation.Config(null, null, null, null, java.util.List.of());
        assertTrue(Activation.ipAutorisee(sansPlage, "1.2.3.4"));                              // aucune restriction
        var cfg = new Activation.Config(null, null, null, null, java.util.List.of("192.168.", "10.0.0."));
        assertTrue(Activation.ipAutorisee(cfg, "192.168.1.50"));                               // dans la plage
        assertTrue(Activation.ipAutorisee(cfg, "10.0.0.7"));
        assertFalse(Activation.ipAutorisee(cfg, "8.8.8.8"));                                   // hors plage
        assertFalse(Activation.ipAutorisee(cfg, null));                                        // IP inconnue + plage définie
    }
}
