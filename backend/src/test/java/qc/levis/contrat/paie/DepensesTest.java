package qc.levis.contrat.paie;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.paie.domain.Depenses;

/** Tests purs des dépenses portées (TPS/TVQ, déductibilité, CTI/RTI récupérables). */
class DepensesTest {

    @Test
    void taxes_de_ht_appliquent_tps_5_et_tvq_9975() {
        Depenses.Taxes t = Depenses.taxesDeHT(100);
        assertEquals(5.0, t.gst(), 0.001);
        assertEquals(9.98, t.qst(), 0.001); // 9,975 arrondi au cent (r2)
    }

    @Test
    void repas_sont_deductibles_a_50_pourcent() {
        assertEquals(50.0, Depenses.deductibiliteParDefaut("meals"), 0.001);
        assertEquals(100.0, Depenses.deductibiliteParDefaut("office"), 0.001);
    }

    @Test
    void sommaire_recupere_les_taxes_au_prorata_deductible() {
        Depenses.Depense repas = new Depenses.Depense(null, "2026-01-10", "meals", "Lunch", 100, 5, 9.975, 50,
                null, null, null, null, null, null, null, null, null, null);
        Depenses.Sommaire s = Depenses.sommaire(List.of(repas), 0.3);
        assertEquals(100, s.totalHT(), 0.01);
        assertEquals(50, s.deductibleExpense(), 0.01);          // 50 % de 100
        assertEquals(2.5, s.recoverableGST(), 0.01);            // 50 % de 5
        assertEquals(4.99, s.recoverableQST(), 0.01);           // 50 % de 9,975
        assertEquals(15, s.estimatedTaxSaving(), 0.01);         // 50 × 0,3
    }
}
