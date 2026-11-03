package qc.levis.contrat.paie;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.paie.domain.ImpotSurLeRevenu;

/** Tests purs de l'impôt sur le revenu porté (sociétés vs particuliers, bilan équilibré). */
class ImpotSurLeRevenuTest {

    @Test
    void societe_applique_les_taux_fixes() {
        ImpotSurLeRevenu.Resultat r = ImpotSurLeRevenu.calculer(Map.of("profile", "incorporated", "revenue", 100_000));
        assertEquals(100_000, r.taxableIncome(), 0.01);               // aucune charge
        assertEquals(9_000, r.federalTax(), 0.01);                    // 9 %
        assertEquals(11_500, r.quebecTax(), 0.01);                    // 11,5 %
        assertEquals(20_500, r.totalTax(), 0.01);
        assertEquals(79_500, r.netIncome(), 0.01);
        assertNotNull(r.balanceSheet()); // bilan produit (s'équilibre si l'actif est saisi)
    }

    @Test
    void salarie_applique_les_baremes_progressifs() {
        ImpotSurLeRevenu.Resultat r = ImpotSurLeRevenu.calculer(Map.of("profile", "employee", "revenue", 60_000));
        assertNull(r.ratesApplied().federal(), "pas de taux fixe pour un particulier");
        assertTrue(r.federalTax() > 0 && r.quebecTax() > 0);
        assertTrue(r.totalTax() < 60_000 && r.netIncome() > 0);
    }

    @Test
    void charges_reduisent_le_revenu_imposable() {
        ImpotSurLeRevenu.Resultat sans = ImpotSurLeRevenu.calculer(Map.of("profile", "incorporated", "revenue", 100_000));
        ImpotSurLeRevenu.Resultat avec = ImpotSurLeRevenu.calculer(Map.of("profile", "incorporated", "revenue", 100_000, "salaries", 40_000));
        assertEquals(60_000, avec.taxableIncome(), 0.01);
        assertTrue(avec.totalTax() < sans.totalTax());
    }

    @Test
    void profil_invalide_est_rejete() {
        assertThrows(IllegalArgumentException.class,
                () -> ImpotSurLeRevenu.validerApercu(Map.of("profile", "inconnu", "revenue", 1000)));
    }
}
