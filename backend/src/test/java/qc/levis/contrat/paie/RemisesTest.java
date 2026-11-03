package qc.levis.contrat.paie;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.paie.domain.Remises;

/** Tests purs des remises portées (fréquence selon la moyenne, échéancier mensuel/trimestriel). */
class RemisesTest {

    @Test
    void frequence_selon_la_moyenne_mensuelle() {
        assertEquals("accelerated2", Remises.frequence(120_000, false)); // ≥ 100 000
        assertEquals("accelerated1", Remises.frequence(30_000, false));  // ≥ 25 000
        assertEquals("quarterly", Remises.frequence(500, true));         // < 1000 + conformité parfaite
        assertEquals("regular", Remises.frequence(500, false));          // sans conformité → régulier
    }

    @Test
    void echeancier_mensuel_a_douze_periodes() {
        List<Remises.Echeance> e = Remises.echeancier("RQ", "regular", 2026);
        assertEquals(12, e.size());
        assertEquals("2026-01-01", e.get(0).periodStart());
    }

    @Test
    void echeancier_trimestriel_a_quatre_periodes() {
        assertEquals(4, Remises.echeancier("CRA", "quarterly", 2026).size());
    }

    @Test
    void annee_invalide_est_rejetee() {
        assertThrows(IllegalArgumentException.class, () -> Remises.echeancier("RQ", "regular", 1800));
    }
}
