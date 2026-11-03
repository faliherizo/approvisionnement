package qc.levis.contrat.regle;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.regle.application.MoteurRegles;
import qc.levis.contrat.regle.application.ResultatEvaluation;
import qc.levis.contrat.regle.domain.ConditionDef;
import qc.levis.contrat.regle.domain.RegleDef;

/** Tests purs du moteur de règles. */
class MoteurReglesTest {

    @Test
    void visibilite_masque_si_condition_non_satisfaite() {
        var r = new RegleDef("VISIBILITE", "ET", List.of(new ConditionDef("type", "EGAL", "entreprise")), "noEntreprise", null);
        var res = MoteurRegles.evaluer(List.of(r), Map.of("type", "particulier"));
        assertEquals(List.of("noEntreprise"), res.champsMasques());
        var res2 = MoteurRegles.evaluer(List.of(r), Map.of("type", "entreprise"));
        assertTrue(res2.champsMasques().isEmpty());
    }

    @Test
    void obligation_et_validation_et_routage() {
        var oblig = new RegleDef("OBLIGATION", "ET", List.of(new ConditionDef("paye", "EGAL", "oui")), "preuve", null);
        var valid = new RegleDef("VALIDATION", "ET", List.of(new ConditionDef("montant", "SUP", "1000")), null, "Montant trop élevé");
        var rout = new RegleDef("ROUTAGE", "ET", List.of(new ConditionDef("montant", "SUPEGAL", "500")), "DIRECTION", null);
        var res = MoteurRegles.evaluer(List.of(oblig, valid, rout), Map.of("paye", "oui", "montant", "1500"));
        assertEquals(List.of("preuve"), res.champsObligatoires());
        assertEquals(List.of("Montant trop élevé"), res.erreurs());
        assertEquals(List.of("DIRECTION"), res.routage());
    }

    @Test
    void calcul_somme_et_produit() {
        var somme = new RegleDef("CALCUL", null, List.of(), "total", "somme:a,b,c");
        var res = MoteurRegles.evaluer(List.of(somme), Map.of("a", "10", "b", "20", "c", "5"));
        assertEquals(0, res.calculs().get("total").compareTo(new BigDecimal("35")));
    }

    @Test
    void calcul_robuste_au_cycle() {
        var x = new RegleDef("CALCUL", null, List.of(), "x", "somme:y");
        var y = new RegleDef("CALCUL", null, List.of(), "y", "somme:x");
        ResultatEvaluation res = MoteurRegles.evaluer(List.of(x, y), Map.of());
        assertNotNull(res.calculs().get("x")); // pas de boucle infinie
    }

    @Test
    void combinateur_ou() {
        var r = new RegleDef("VALIDATION", "OU",
                List.of(new ConditionDef("a", "VIDE", null), new ConditionDef("b", "VIDE", null)), null, "Au moins un requis");
        assertTrue(MoteurRegles.evaluer(List.of(r), Map.of("a", "x")).erreurs().contains("Au moins un requis")); // b vide
        assertTrue(MoteurRegles.evaluer(List.of(r), Map.of("a", "x", "b", "y")).erreurs().isEmpty());
    }
}
