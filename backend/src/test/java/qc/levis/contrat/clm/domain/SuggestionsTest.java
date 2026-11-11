package qc.levis.contrat.clm.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Saisie assistée (F-04) : ordre de proposition, valeurs écartées et plafond. */
class SuggestionsTest {

    private static Map<String, Preremplissage.Valeur> jeu(Object valeur) {
        return Map.of("demandeur", new Preremplissage.Valeur(valeur, Preremplissage.Provenance.MANUEL, Instant.EPOCH));
    }

    @Test
    void la_plus_frequente_d_abord_puis_la_plus_recente() {
        List<Map<String, Preremplissage.Valeur>> jeux = List.of(jeu("Récente"), jeu("Acme"), jeu("Ancienne"), jeu(" Acme "));
        assertEquals(List.of("Acme", "Récente", "Ancienne"), Suggestions.pour("demandeur", jeux, null));
    }

    @Test
    void la_valeur_actuelle_les_vides_et_les_champs_absents_sont_ecartes() {
        List<Map<String, Preremplissage.Valeur>> jeux = List.of(jeu("Acme"), jeu(" "), jeu(null), Map.of(), jeu("Boréal"));
        assertEquals(List.of("Boréal"), Suggestions.pour("demandeur", jeux, " Acme"));
    }

    @Test
    void au_plus_huit_propositions() {
        List<Map<String, Preremplissage.Valeur>> jeux = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            jeux.add(jeu("Valeur " + i));
        }
        assertEquals(Suggestions.MAX, Suggestions.pour("demandeur", jeux, null).size());
        assertEquals("Valeur 0", Suggestions.pour("demandeur", jeux, null).get(0));
    }
}
