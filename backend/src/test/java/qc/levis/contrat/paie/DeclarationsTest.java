package qc.levis.contrat.paie;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.paie.domain.Declarations;
import qc.levis.contrat.paie.domain.Depenses;
import qc.levis.contrat.paie.domain.MoteurPaie;

/** Tests purs des déclarations portées (FPZ-500, retenues à la source, Relevé 1 + T4). */
class DeclarationsTest {

    private static final Declarations.Periode P = new Declarations.Periode("2026-01-01", "2026-12-31");

    private MoteurPaie.BulletinEmis bulletin(String emp, String payDate, double gross) {
        MoteurPaie.Bulletin slip = MoteurPaie.calculer(MoteurPaie.Entree.simple(gross, "biweekly", MoteurPaie.Ytd.ZERO), MoteurPaie.Taux.DE_2025);
        return new MoteurPaie.BulletinEmis(null, emp, null, null, payDate, slip, null, null, "CAD", "CA", "preview");
    }

    @Test
    void fpz500_retranche_les_cti_rti_des_depenses() {
        Depenses.Depense d = new Depenses.Depense(null, "2026-03-01", "office", "Fournitures", 100, 5, 9.975, 100,
                null, null, null, null, null, null, null, null, null, null);
        Declarations.RapportTaxes r = Declarations.taxes(P, List.of(d), 200, 400);
        assertEquals(5, r.gstITC(), 0.01);
        assertEquals(9.975, r.qstITR(), 0.01);
        assertEquals(195, r.gstNet(), 0.01);        // 200 perçue − 5 récupérable
        assertEquals(390.025, r.qstNet(), 0.01);    // 400 − 9,975
    }

    @Test
    void retenues_somment_parts_employe_et_employeur() {
        Declarations.RapportRetenues r = Declarations.retenues(P, List.of(bulletin("e1", "2026-06-15", 2000), bulletin("e1", "2026-06-29", 2000)));
        assertEquals(2, r.payRunCount());
        assertTrue(r.totalGross() > 0 && r.quebecIncomeTax() > 0);
        assertTrue(r.totalToRevenuQuebec() > 0 && r.totalToCRA() > 0);
    }

    @Test
    void fin_annee_produit_releve1_et_t4_de_la_personne() {
        Declarations.SyntheseAnnuelle s = Declarations.finDAnnee("e1", List.of(bulletin("e1", "2026-06-15", 2000), bulletin("e2", "2026-06-15", 3000)), 2026);
        assertEquals(1, s.payRunCount(), "seulement les bulletins de e1");
        assertTrue(s.rl1().caseA_employmentIncome() > 0);
        assertTrue(s.t4().box14_employmentIncome() > 0);
        assertEquals(s.rl1().caseA_employmentIncome(), s.t4().box14_employmentIncome(), 0.01);
    }
}
