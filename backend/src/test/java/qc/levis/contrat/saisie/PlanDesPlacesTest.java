package qc.levis.contrat.saisie;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.saisie.domain.*;

class PlanDesPlacesTest {

    static final ChampAChoix ACTIVITE = new ChampAChoix("activite", "Activité", List.of("Atelier", "Formation", "Visite"),
            Map.of("Atelier", 2, "Formation", 1));
    static final ChampAChoix JOURS = new ChampAChoix("jours", "Jours", List.of("Lundi", "Mardi"), Map.of());

    @Test
    void les_places_se_comptent_par_choix_reponse_unique_ou_multiple() {
        PlanDesPlaces plan = PlanDesPlaces.compter(List.of(ACTIVITE, JOURS), List.of(
                Map.of("activite", "Atelier", "jours", List.of("Lundi", "Mardi")),
                Map.of("activite", "Formation", "jours", List.of("Lundi")),
                Map.of("activite", " ")));
        PlaceChoix atelier = plan.places().get(0);
        assertEquals(List.of("activite", "Atelier", 2, 1, 1), List.of(atelier.champ(), atelier.choix(), atelier.capacite(), atelier.inscrits(), atelier.restantes()));
        assertFalse(atelier.complet());
        PlaceChoix formation = plan.places().get(1);
        assertTrue(formation.complet());
        assertEquals(0, formation.restantes());
        PlaceChoix visite = plan.places().get(2);
        assertNull(visite.capacite());
        assertNull(visite.restantes());
        assertFalse(visite.complet());
        assertEquals(2, plan.places().get(3).inscrits());
        assertTrue(ACTIVITE.estLimite());
        assertFalse(JOURS.estLimite());
    }

    @Test
    void une_fiche_qui_retient_un_choix_complet_est_refusee() {
        PlanDesPlaces plan = PlanDesPlaces.compter(List.of(ACTIVITE), List.of(Map.of("activite", "Formation")));
        PlanDesPlaces.ChoixComplet refus = assertThrows(PlanDesPlaces.ChoixComplet.class,
                () -> plan.exigerDesPlaces(Map.of("activite", "Formation")));
        assertTrue(refus.getMessage().contains("« Formation » (Activité) est complet : 1 place(s)"));
        assertDoesNotThrow(() -> plan.exigerDesPlaces(Map.of("activite", "Atelier")));
        assertDoesNotThrow(() -> plan.exigerDesPlaces(Map.of()));
    }

    @Test
    void une_capacite_negative_est_refusee_et_les_listes_sont_immuables() {
        assertThrows(IllegalArgumentException.class, () -> new ChampAChoix("a", "A", List.of("x"), Map.of("x", -1)));
        assertThrows(UnsupportedOperationException.class, () -> ACTIVITE.choix().add("y"));
        InventaireChoix inv = new InventaireChoix(new PlaceChoix("a", "A", "x", 1, 1),
                List.of(new InventaireChoix.Inscription("RH-1", "SOUMIS", Instant.EPOCH, "a@b.ca")));
        assertThrows(UnsupportedOperationException.class, () -> inv.inscriptions().clear());
    }

    @Test
    void seules_les_fiches_actives_occupent_une_place() {
        for (StatutSaisie s : StatutSaisie.values()) {
            boolean attendu = switch (s) {
                case SOUMIS, EN_TRAITEMENT, A_COMPLETER, VALIDE, CLOTURE -> true;
                case BROUILLON, REJETE, ANNULE -> false;
            };
            assertEquals(attendu, s.occupeUnePlace(), s.name());
        }
    }
}
