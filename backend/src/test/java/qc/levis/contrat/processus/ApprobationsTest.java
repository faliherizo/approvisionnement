package qc.levis.contrat.processus;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.processus.domain.Approbations;

/**
 * Tests purs du moteur d'approbations multi-niveaux porté de Nexu VINA (routage par règle field/operator/
 * value/level). Complète le workflow séquentiel d'AP-3 pour les circuits conditionnels riches.
 */
class ApprobationsTest {

    private Approbations.Regle regle(String field, String op, Object val, int level) {
        return Approbations.validerRegle("achat", field, op, val, level, "APPRO", null);
    }

    @Test void valider_regle_rejette_un_operateur_inconnu() {
        assertThrows(IllegalArgumentException.class,
                () -> Approbations.validerRegle("achat", "montant", "between", 1000, 1, "APPRO", null));
    }

    @Test void valider_regle_exige_un_approbateur() {
        assertThrows(IllegalArgumentException.class,
                () -> Approbations.validerRegle("achat", "montant", "gte", 1000, 1, null, null));
    }

    @Test void correspond_comparaisons_numeriques_et_egalite() {
        assertTrue(Approbations.correspond(regle("montant", "gte", 1000, 1), Map.of("montant", 1500)));
        assertFalse(Approbations.correspond(regle("montant", "gte", 1000, 1), Map.of("montant", 500)));
        assertTrue(Approbations.correspond(regle("montant", "lte", 1000, 1), Map.of("montant", 999)));
        assertTrue(Approbations.correspond(regle("type", "eq", "urgent", 1), Map.of("type", "urgent")));
        assertTrue(Approbations.correspond(regle("type", "neq", "urgent", 1), Map.of("type", "normal")));
        assertFalse(Approbations.correspond(regle("montant", "gte", 1000, 1), Map.of())); // champ absent
    }

    @Test void selectionner_ordonne_les_etapes_par_niveau_une_par_niveau() {
        var r1 = regle("montant", "gte", 0, 1);       // niveau 1 : gestionnaire
        var r2 = regle("montant", "gte", 10000, 2);   // niveau 2 : finance si > 10k
        // sous le seuil : seule l'étape 1 s'applique
        assertEquals(1, Approbations.selectionner(List.of(r1, r2), Map.of("montant", 500)).size());
        // au-dessus : deux étapes ordonnées 1 puis 2
        List<Approbations.Etape> etapes = Approbations.selectionner(List.of(r2, r1), Map.of("montant", 25000));
        assertEquals(List.of(1, 2), etapes.stream().map(Approbations.Etape::level).toList());
    }

    @Test void doit_relancer_au_dela_de_la_tolerance() {
        assertTrue(Approbations.doitRelancer(1000, 1200, 0.1));   // +20 % > 10 %
        assertFalse(Approbations.doitRelancer(1000, 1050, 0.1));  // +5 % <= 10 %
        assertTrue(Approbations.doitRelancer(0, 100, 0.1));       // de zéro : toute variation relance
    }
}
