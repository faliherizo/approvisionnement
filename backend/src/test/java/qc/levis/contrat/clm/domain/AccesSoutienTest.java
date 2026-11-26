package qc.levis.contrat.clm.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

/** Accès de soutien de l'exploitant (Annexe 10, SEC-17) : motivé, borné, accordé par l'organisation, expirant, révocable. */
class AccesSoutienTest {

    private static final Instant T0 = Instant.parse("2026-11-25T12:00:00Z");

    @Test
    void une_demande_exige_un_motif_et_une_duree_bornee() {
        assertThrows(IllegalArgumentException.class, () -> AccesSoutien.demander(1L, "sa", null, 2, T0));
        assertThrows(IllegalArgumentException.class, () -> AccesSoutien.demander(1L, "sa", "  ", 2, T0));
        assertThrows(IllegalArgumentException.class, () -> AccesSoutien.demander(1L, "sa", "x".repeat(501), 2, T0));
        assertThrows(IllegalArgumentException.class, () -> AccesSoutien.demander(1L, "sa", "Billet", 0, T0));
        assertThrows(IllegalArgumentException.class, () -> AccesSoutien.demander(1L, "sa", "Billet", 9, T0));
        AccesSoutien a = AccesSoutien.demander(1L, "sa@vina", " Billet 42 ", 8, T0);
        assertEquals("Billet 42", a.getMotif());
        assertEquals(AccesSoutien.Statut.DEMANDE, a.statut(T0));
        assertFalse(a.actif(T0), "une demande n'ouvre rien");
        assertEquals(1L, a.getTenantId());
        assertEquals("sa@vina", a.getDemandeur());
        assertEquals(8, a.getDureeHeures());
        assertEquals(T0, a.getDemandeLe());
        assertNull(a.getId());
    }

    @Test
    void l_acces_accorde_court_depuis_la_decision_puis_expire() {
        AccesSoutien a = AccesSoutien.demander(1L, "sa", "Billet", 2, T0);
        Instant accord = T0.plus(Duration.ofHours(1));
        a.accorder("admin.affaires", accord);
        assertEquals(accord.plus(Duration.ofHours(2)), a.getExpireLe());
        assertEquals("admin.affaires", a.getDecidePar());
        assertEquals(accord, a.getDecideLe());
        assertTrue(a.actif(accord.plus(Duration.ofMinutes(119))));
        assertEquals(AccesSoutien.Statut.ACCORDE, a.statut(accord));
        assertFalse(a.actif(a.getExpireLe()), "l'échéance ferme l'accès");
        assertEquals(AccesSoutien.Statut.EXPIRE, a.statut(a.getExpireLe()));
        assertThrows(IllegalStateException.class, () -> a.revoquer("x", a.getExpireLe()), "rien à révoquer après l'échéance");
        assertThrows(IllegalStateException.class, () -> a.accorder("x", accord), "une décision ne se reprend pas");
        assertThrows(IllegalStateException.class, () -> a.refuser("x", accord));
        a.consulter();
        a.consulter();
        assertEquals(2, a.getConsultations());
    }

    @Test
    void l_organisation_refuse_ou_revoque() {
        AccesSoutien refuse = AccesSoutien.demander(1L, "sa", "Billet", 1, T0);
        refuse.refuser("admin.affaires", T0);
        assertEquals(AccesSoutien.Statut.REFUSE, refuse.statut(T0));
        assertNull(refuse.getExpireLe());
        assertThrows(IllegalStateException.class, () -> refuse.revoquer("x", T0));

        AccesSoutien revoque = AccesSoutien.demander(1L, "sa", "Billet", 4, T0);
        revoque.accorder("admin.affaires", T0);
        revoque.revoquer("admin.affaires", T0.plusSeconds(60));
        assertEquals(AccesSoutien.Statut.REVOQUE, revoque.statut(T0.plusSeconds(60)));
        assertEquals(T0.plusSeconds(60), revoque.getExpireLe());
        assertFalse(revoque.actif(T0.plusSeconds(61)));
    }
}
