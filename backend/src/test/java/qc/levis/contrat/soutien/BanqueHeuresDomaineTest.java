package qc.levis.contrat.soutien;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.soutien.domain.*;

class BanqueHeuresDomaineTest {

    static final LocalDate D = LocalDate.of(2026, 1, 1);
    static final LocalDate F = LocalDate.of(2026, 12, 31);
    static final Instant T = Instant.EPOCH;

    static BigDecimal h(String s) { return new BigDecimal(s); }

    @Test
    void la_banque_couvre_une_periode_valide() {
        BanqueHeures b = new BanqueHeures(3L, " Soutien 2026 ", D, F, T);
        assertEquals("Soutien 2026", b.getNom());
        assertDoesNotThrow(() -> b.exigerDansPeriode(D));
        assertDoesNotThrow(() -> b.exigerDansPeriode(F));
        assertThrows(IllegalArgumentException.class, () -> b.exigerDansPeriode(F.plusDays(1)));
        assertThrows(IllegalArgumentException.class, () -> b.exigerDansPeriode(D.minusDays(1)));
        assertThrows(IllegalArgumentException.class, () -> new BanqueHeures(3L, " ", D, F, T));
        assertThrows(IllegalArgumentException.class, () -> new BanqueHeures(3L, null, D, F, T));
        assertThrows(IllegalArgumentException.class, () -> new BanqueHeures(3L, "N", F, D, T));
        assertThrows(IllegalArgumentException.class, () -> new BanqueHeures(3L, "N", null, F, T));
        assertThrows(IllegalArgumentException.class, () -> new BanqueHeures(3L, "N", D, null, T));
        assertEquals(List.of(3L, D, F, T), List.of(b.getTenantId(), b.getDebut(), b.getFin(), b.getCreeLe()));
    }

    @Test
    void les_heures_se_comptent_au_quart_d_heure_et_sont_decrites() {
        assertEquals(h("1.75"), Mouvement.achat(1L, h("1.75"), D, "Achat", "sa", T).getHeures());
        assertThrows(IllegalArgumentException.class, () -> Mouvement.achat(1L, h("1.1"), D, "Achat", "sa", T));
        assertThrows(IllegalArgumentException.class, () -> Mouvement.achat(1L, h("0"), D, "Achat", "sa", T));
        assertThrows(IllegalArgumentException.class, () -> Mouvement.achat(1L, null, D, "Achat", "sa", T));
        assertThrows(IllegalArgumentException.class, () -> Mouvement.achat(1L, h("1"), D, " ", "sa", T));
        assertThrows(IllegalArgumentException.class, () -> Mouvement.achat(1L, h("1"), D, null, "sa", T));
        assertThrows(IllegalArgumentException.class, () -> Mouvement.consommation(1L, null, h("1"), D, "x", "i", "sa", T));
        assertThrows(IllegalArgumentException.class, () -> Mouvement.consommation(1L, CategorieService.ANALYSE, h("1"), D, "x", " ", "sa", T));
        assertThrows(IllegalArgumentException.class, () -> Mouvement.consommation(1L, CategorieService.ANALYSE, h("1"), D, "x", null, "sa", T));
        assertThrows(IllegalArgumentException.class, () -> Mouvement.correction(1L, null, h("1"), D, "x", "sa", T));
        Mouvement c = Mouvement.consommation(1L, CategorieService.FORMATION, h("2"), D, " Atelier ", " Julie ", "sa", T);
        assertEquals(List.of(1L, Mouvement.Type.CONSOMMATION, CategorieService.FORMATION, "Atelier", "Julie", "sa", D, T),
                List.of(c.getBanqueId(), c.getType(), c.getCategorie(), c.getDescription(), c.getIntervenant(), c.getSaisiPar(), c.getDate(), c.getSaisiLe()));
        assertNull(c.getId());
    }

    @Test
    void le_solde_deduit_les_consommations_nettes_des_corrections() {
        Solde s = Solde.de(List.of(
                Mouvement.achat(1L, h("100"), D, "Achat", "sa", T),
                Mouvement.achat(1L, h("20"), D, "Ajout", "sa", T),
                Mouvement.consommation(1L, CategorieService.ANALYSE, h("10.5"), D, "x", "i", "sa", T),
                Mouvement.consommation(1L, CategorieService.PROGRAMMATION, h("30"), D, "x", "i", "sa", T),
                Mouvement.correction(1L, CategorieService.PROGRAMMATION, h("5"), D, "saisi en trop", "sa", T)));
        assertEquals(h("120.00"), s.achetees());
        assertEquals(h("35.50"), s.consommees());
        assertEquals(h("84.50"), s.restantes());
        assertEquals(h("25.00"), s.parCategorie().get(CategorieService.PROGRAMMATION));
        assertEquals(h("0.00"), s.parCategorie().get(CategorieService.RAPPORTS));
        assertDoesNotThrow(() -> s.exigerDisponible(h("84.50")));
        assertThrows(Solde.SoldeInsuffisant.class, () -> s.exigerDisponible(h("84.75")));
        assertDoesNotThrow(() -> s.exigerCorrigeable(CategorieService.ANALYSE, h("10.50")));
        assertThrows(IllegalArgumentException.class, () -> s.exigerCorrigeable(CategorieService.RAPPORTS, h("0.25")));
    }
}
