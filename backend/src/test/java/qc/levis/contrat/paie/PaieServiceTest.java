package qc.levis.contrat.paie;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.paie.application.PaieService;
import qc.levis.contrat.paie.application.dto.ApercuPaieRequest;

/** Tests du service de paie (orchestration des moteurs purs ; mapping des requêtes Map). */
class PaieServiceTest {

    private final PaieService service = new PaieService();

    @Test void apercu_salarie_retient_et_net_inferieur() {
        Map<String, Object> b = service.apercu(new ApercuPaieRequest(2000, "biweekly", false));
        assertTrue((double) b.get("netPay") < 2000);
        assertTrue((double) b.get("qpp") > 0);
    }

    @Test void apercu_frequence_vide_retombe_sur_biweekly() {
        Map<String, Object> b = service.apercu(new ApercuPaieRequest(2000, null, false));
        assertEquals(26, b.get("periodsPerYear"));
    }

    @Test void impot_delegue_et_valide() {
        Map<String, Object> r = service.impot(Map.of("profile", "incorporated", "revenue", 100_000));
        assertEquals(9_000.0, r.get("federalTax"));
    }

    @Test void taxes_construit_les_depenses_depuis_la_requete() {
        var r = service.taxes(Map.of("start", "2026-01-01", "end", "2026-12-31", "gstCollected", 200, "qstCollected", 400,
                "depenses", List.of(Map.of("date", "2026-03-01", "category", "office", "amountHT", 100, "gst", 5, "qst", 9.98))));
        assertEquals(195.0, r.gstNet(), 0.01);
    }

    @Test void retenues_recalcule_les_bulletins_depuis_les_entrees() {
        var r = service.retenues(Map.of("start", "2026-01-01", "end", "2026-12-31",
                "bulletins", List.of(Map.of("employeeId", "e1", "payDate", "2026-06-15", "grossSalary", 2000, "frequency", "biweekly"))));
        assertEquals(1, r.payRunCount());
        assertTrue(r.totalToRevenuQuebec() > 0);
    }

    @Test void fin_annee_produit_releve1_et_t4() {
        var s = service.finDAnnee(Map.of("employeeId", "e1", "annee", 2026,
                "bulletins", List.of(Map.of("employeeId", "e1", "payDate", "2026-06-15", "grossSalary", 2000, "frequency", "biweekly"))));
        assertEquals(1, s.payRunCount());
        assertTrue(s.t4().box14_employmentIncome() > 0);
    }

    @Test void echeancier_remises_mensuel() {
        assertEquals(12, service.echeancierRemises("RQ", "regular", 2026).size());
    }

    @Test void taux_et_frequences_exposes() {
        assertEquals(2025, service.taux().get("year"));
        assertFalse(service.frequences().isEmpty());
    }
}
