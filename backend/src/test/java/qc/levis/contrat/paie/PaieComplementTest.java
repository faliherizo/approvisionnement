package qc.levis.contrat.paie;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.paie.domain.AvantagesSociaux;
import qc.levis.contrat.paie.domain.Depenses;
import qc.levis.contrat.paie.domain.ElementsDeduction;
import qc.levis.contrat.paie.domain.MoteurPaie;

/** Tests complémentaires des moteurs de paie portés : avantages, éléments de déduction, sérialiseurs, dépenses. */
class PaieComplementTest {

    // --- Avantages sociaux ---
    @Test void avantage_rrsp_match_reduit_les_deux_impots() {
        var a = new AvantagesSociaux.Avantage("1", "e1", "rrsp_match", 100, 100, false, "2026-01-01", null, "t");
        MoteurPaie.ElementResolu r = a.element(26);
        assertTrue(r.reducesFederalTax() && r.reducesQuebecTax());
        assertEquals(MoteurPaie.arrondir(100 * 12 / 26.0), r.employeeAmount(), 0.001);
    }

    @Test void avantages_filtres_par_date_de_vigueur() {
        var actif = new AvantagesSociaux.Avantage("1", "e1", "transit", 0, 50, true, "2026-01-01", null, "t");
        var expire = new AvantagesSociaux.Avantage("2", "e1", "dental", 0, 30, false, "2020-01-01", "2020-12-31", "t");
        List<MoteurPaie.ElementResolu> els = AvantagesSociaux.elements(List.of(actif, expire), "biweekly", "2026-06-15");
        assertEquals(1, els.size());
    }

    @Test void avantage_valide_signale_les_erreurs() {
        assertTrue(AvantagesSociaux.valider(Map.of("kind", "transit", "employeeMonthly", 0, "employerMonthly", 50, "startDate", "2026-01-01"), true).isEmpty());
        assertTrue(AvantagesSociaux.valider(Map.of(), false).size() >= 3);
        var a = AvantagesSociaux.nouveau("1", "t", Map.of("employeeId", "e1", "kind", "transit", "employeeMonthly", 0, "employerMonthly", 50, "startDate", "2026-01-01"));
        assertEquals("transit", a.kind());
    }

    // --- Éléments de déduction ---
    @Test void element_percent_calcule_sur_le_brut() {
        var e = new ElementsDeduction.Element("1", "t", "e1", "rrsp", "percent", 5, "employee", null,
                true, true, false, false, null, "REER", true, null, null, null, null);
        List<MoteurPaie.ElementResolu> r = ElementsDeduction.resoudre(List.of(e), 2000, 26, "2026-06-15");
        assertEquals(1, r.size());
        assertEquals(100.0, r.get(0).employeeAmount(), 0.001); // 5 % de 2000
    }

    @Test void element_inactif_est_ignore() {
        var e = new ElementsDeduction.Element("1", "t", "e1", "union_dues", "fixed", 25, "employee", null,
                true, false, false, false, null, "Syndicat", false, null, null, null, null);
        assertTrue(ElementsDeduction.resoudre(List.of(e), 2000, 26, "2026-06-15").isEmpty());
    }

    @Test void traitements_de_reference_connus() {
        assertTrue(ElementsDeduction.TYPES.contains("rrsp"));
        assertTrue(ElementsDeduction.TRAITEMENTS.get("union_dues").reducesFederalTax());
        assertFalse(ElementsDeduction.TRAITEMENTS.get("union_dues").reducesQuebecTax());
    }

    // --- Sérialiseurs (vue / depuis) ---
    @Test void bulletin_vue_et_relecture() {
        MoteurPaie.Bulletin b = MoteurPaie.calculer(MoteurPaie.Entree.simple(2000, "biweekly", MoteurPaie.Ytd.ZERO), MoteurPaie.Taux.DE_2025);
        Map<String, Object> m = b.vue();
        assertTrue(m.containsKey("netPay") && m.containsKey("ytdAfter"));
        MoteurPaie.Bulletin relu = MoteurPaie.Bulletin.depuis(m);
        assertEquals(b.netPay(), relu.netPay(), 0.001);
    }

    @Test void ytd_vue_et_depuis() {
        MoteurPaie.Ytd y = new MoteurPaie.Ytd(100, 6, 0, 100, 1, 100, 0.5);
        MoteurPaie.Ytd relu = MoteurPaie.Ytd.depuis(y.vue());
        assertEquals(100, relu.qppPensionable(), 0.001);
        assertSame(MoteurPaie.Ytd.ZERO, MoteurPaie.Ytd.depuis(null));
    }

    // --- Dépenses (document, écriture, saisie rapide) ---
    @Test void saisie_rapide_calcule_les_taxes_si_absentes() {
        Depenses.SaisieRapide s = Depenses.saisieRapide(Map.of("date", "2026-03-01", "category", "office", "description", "Papeterie", "amountHT", 100));
        assertEquals(5.0, s.gst(), 0.001);
        assertEquals(100.0, s.deductiblePct(), 0.001);
    }

    @Test void document_totalise_les_lignes_et_produit_une_ecriture_equilibree() {
        Depenses.Depense d = Depenses.document("d1", "t", Map.of("date", "2026-03-01",
                "lines", List.of(Map.of("category", "office", "amountHT", 100), Map.of("category", "meals", "amountHT", 50))), "e1");
        assertEquals(150.0, d.amountHT(), 0.01);
        Depenses.EcritureDepense ec = Depenses.ecriture(d);
        double debit = ec.lignes().stream().mapToDouble(Depenses.LigneGrandLivre::debit).sum();
        double credit = ec.lignes().stream().mapToDouble(Depenses.LigneGrandLivre::credit).sum();
        assertEquals(debit, credit, 0.01); // écriture équilibrée
    }

    @Test void document_refuse_sans_ligne() {
        assertThrows(IllegalArgumentException.class, () -> Depenses.document("d1", "t", Map.of("date", "2026-03-01", "lines", List.of()), "e1"));
    }

    @Test void recherche_depense_insensible_a_la_casse() {
        Depenses.Depense d = Depenses.document("d1", "t", Map.of("date", "2026-03-01", "payee", "Bureau XYZ",
                "lines", List.of(Map.of("category", "office", "amountHT", 100))), "e1");
        assertTrue(Depenses.correspond(d, "bureau"));
        assertTrue(Depenses.correspond(d, ""));
    }
}
