package qc.levis.contrat.auth;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.auth.domain.LimitesSession;
import qc.levis.contrat.auth.domain.LimitesSession.Verdict;
import qc.levis.contrat.auth.domain.PolitiqueSession;

/** ST-01 : fin de session par inactivité et par durée maximale, renouvellement borné, réglages dangereux refusés. */
class LimitesSessionTest {

    private static final Instant T0 = Instant.parse("2026-11-22T08:00:00Z");
    private final LimitesSession l = new LimitesSession(Duration.ofMinutes(15), Duration.ofHours(8));

    @Test
    void une_session_active_reste_valide_puis_se_termine_par_inactivite() {
        assertEquals(Verdict.VALIDE, l.verifier(T0, T0, T0.plusSeconds(14 * 60)));
        assertEquals(Verdict.INACTIVE, l.verifier(T0, T0, T0.plusSeconds(15 * 60)));
        assertEquals(Verdict.VALIDE, l.verifier(T0, T0.plusSeconds(3600), T0.plusSeconds(3600 + 600)), "l'activité repousse l'échéance");
    }

    @Test
    void la_duree_maximale_l_emporte_sur_l_activite() {
        Instant presqueFin = T0.plus(Duration.ofHours(8)).minusSeconds(60);
        assertEquals(Verdict.VALIDE, l.verifier(T0, presqueFin, presqueFin.plusSeconds(30)));
        assertEquals(Verdict.EXPIREE, l.verifier(T0, presqueFin, T0.plus(Duration.ofHours(8))));
    }

    @Test
    void un_jeton_renouvele_ne_depasse_jamais_la_duree_maximale() {
        assertEquals(Duration.ofMinutes(15), l.validiteRenouvelee(T0, T0.plusSeconds(60)));
        assertEquals(Duration.ofMinutes(5), l.validiteRenouvelee(T0, T0.plus(Duration.ofHours(8)).minusSeconds(300)));
    }

    @Test
    void les_reglages_dangereux_ou_incoherents_sont_refuses_avec_leur_motif() {
        assertMotif(null, Duration.ofHours(1), "requis");
        assertMotif(Duration.ofMinutes(10), null, "requis");
        assertMotif(Duration.ofMinutes(4), Duration.ofHours(1), "inactivité");
        assertMotif(Duration.ofHours(5), Duration.ofHours(8), "inactivité");
        assertMotif(Duration.ofMinutes(5), Duration.ofMinutes(14), "Durée maximale");
        assertMotif(Duration.ofMinutes(5), Duration.ofHours(25), "Durée maximale");
        assertMotif(Duration.ofMinutes(30), Duration.ofMinutes(20), "dépasser");
        assertDoesNotThrow(() -> new LimitesSession(Duration.ofMinutes(5), Duration.ofMinutes(15)));
        assertDoesNotThrow(() -> new LimitesSession(Duration.ofHours(4), Duration.ofHours(24)));
    }

    private static void assertMotif(Duration inactivite, Duration max, String motif) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> new LimitesSession(inactivite, max));
        assertTrue(e.getMessage().contains(motif), e.getMessage());
    }

    @Test
    void la_politique_d_une_organisation_garde_ses_limites_et_son_auteur() {
        PolitiqueSession p = new PolitiqueSession(7L);
        p.definir(l, "admin.technique@iq.example", T0);
        assertEquals(l, p.limites());
        assertEquals(7L, p.getTenantId());
        assertEquals("admin.technique@iq.example", p.getModifiePar());
        assertEquals(T0, p.getModifieLe());
    }
}
