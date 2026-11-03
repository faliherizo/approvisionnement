package qc.levis.contrat.paie;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import qc.levis.contrat.paie.domain.Baremes;
import qc.levis.contrat.paie.domain.MoteurPaie;

/** Tests purs du moteur de paie porté (RRQ/AE/RQAP/impôt, barèmes 2025). Aucune E/S. */
class MoteurPaieTest {

    @Test
    void bulletin_salarie_retient_les_cotisations_et_le_net_est_inferieur_au_brut() {
        MoteurPaie.Entree e = MoteurPaie.Entree.simple(2000, "biweekly", MoteurPaie.Ytd.ZERO);
        MoteurPaie.Bulletin b = MoteurPaie.calculer(e, MoteurPaie.Taux.DE_2025);

        assertEquals(26, b.periodsPerYear());          // biweekly
        assertTrue(b.qpp() > 0, "RRQ cotisé");
        assertTrue(b.ei() > 0, "AE cotisée");
        assertTrue(b.qpip() > 0, "RQAP cotisé");
        assertTrue(b.federalTax() >= 0 && b.quebecTax() >= 0);
        assertTrue(b.totalEmployeeDeductions() > 0);
        assertTrue(b.netPay() < 2000 && b.netPay() > 0, "net strictement entre 0 et le brut");
        // Charges employeur : AE = 1,4 × part employé ; CNESST et FSS sur le brut.
        assertEquals(MoteurPaie.arrondir(b.ei() * 1.4), b.employerEi(), 0.011);
        assertEquals(MoteurPaie.arrondir(2000 * 0.0178), b.employerCnesst(), 0.011);
    }

    @Test
    void sans_retenues_le_net_egale_le_brut() {
        MoteurPaie.Entree e = new MoteurPaie.Entree(1500, "monthly", MoteurPaie.Ytd.ZERO, 0, 0, 0, java.util.List.of(), true);
        MoteurPaie.Bulletin b = MoteurPaie.calculer(e, MoteurPaie.Taux.DE_2025);
        assertEquals(1500, b.netPay(), 0.001);
        assertEquals(0, b.totalEmployeeDeductions(), 0.001);
        assertEquals(0, b.qpp(), 0.001);
    }

    @Test
    void impot_progressif_est_croissant_et_nul_sous_zero() {
        assertEquals(0, MoteurPaie.impotProgressif(0, Baremes.TRANCHES_FEDERALES), 0.001);
        double bas = MoteurPaie.impotProgressif(40_000, Baremes.TRANCHES_FEDERALES);
        double haut = MoteurPaie.impotProgressif(120_000, Baremes.TRANCHES_FEDERALES);
        assertTrue(haut > bas && bas > 0);
    }

    @Test
    void plafond_rrq_respecte_le_cumul_annuel() {
        // Cumul déjà au max : plus aucune cotisation RRQ sur la période.
        MoteurPaie.Ytd auMax = new MoteurPaie.Ytd(71_300, 4000, 0, 0, 0, 0, 0);
        MoteurPaie.Bulletin b = MoteurPaie.calculer(MoteurPaie.Entree.simple(2000, "biweekly", auMax), MoteurPaie.Taux.DE_2025);
        assertEquals(0, b.qpp(), 0.001);
    }

    @Test
    void frequence_inconnue_est_rejetee() {
        assertThrows(IllegalArgumentException.class,
                () -> MoteurPaie.calculer(MoteurPaie.Entree.simple(1000, "yearly", MoteurPaie.Ytd.ZERO), MoteurPaie.Taux.DE_2025));
    }
}
