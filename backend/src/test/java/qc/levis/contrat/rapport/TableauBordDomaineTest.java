package qc.levis.contrat.rapport;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.rapport.domain.*;
import qc.levis.contrat.rapport.domain.Agregation.Fiche;
import qc.levis.contrat.rapport.domain.Agregation.Point;
import qc.levis.contrat.rapport.domain.Indicateur.Regroupement;
import qc.levis.contrat.rapport.domain.Indicateur.Type;

class TableauBordDomaineTest {

    static final List<Fiche> FICHES = List.of(
            new Fiche("SOUMIS", Instant.parse("2026-10-15T15:00:00Z"), Map.of("activite", "Atelier", "jours", List.of("Lundi", "Mardi"))),
            new Fiche("VALIDE", Instant.parse("2026-11-02T15:00:00Z"), Map.of("activite", "Atelier")),
            new Fiche("BROUILLON", null, Map.of()));

    @Test
    void un_compteur_compte_les_fiches_retenues_par_statut() {
        assertEquals(List.of(new Point("Fiches", 3)), Agregation.calculer(new Indicateur("Total", Type.COMPTEUR, 1L, null, null, null), FICHES));
        assertEquals(List.of(new Point("Fiches", 2)), Agregation.calculer(
                new Indicateur("Actives", Type.COMPTEUR, 1L, Regroupement.AUCUN, null, List.of("SOUMIS", "VALIDE")), FICHES));
    }

    @Test
    void un_histogramme_regroupe_par_statut_ou_par_reponse() {
        assertEquals(List.of(new Point("BROUILLON", 1), new Point("SOUMIS", 1), new Point("VALIDE", 1)),
                Agregation.calculer(new Indicateur("Statuts", Type.BARRES, 1L, Regroupement.STATUT, null, null), FICHES));
        assertEquals(List.of(new Point("(sans réponse)", 1), new Point("Atelier", 2)),
                Agregation.calculer(new Indicateur("Activités", Type.BARRES, 1L, Regroupement.CHAMP, "activite", null), FICHES));
        assertEquals(List.of(new Point("(sans réponse)", 2), new Point("Lundi", 1), new Point("Mardi", 1)),
                Agregation.calculer(new Indicateur("Jours", Type.BARRES, 1L, Regroupement.CHAMP, "jours", null), FICHES));
    }

    @Test
    void une_evolution_regroupe_par_mois_de_soumission_sans_les_brouillons() {
        assertEquals(List.of(new Point("2026-10", 1), new Point("2026-11", 1)),
                Agregation.calculer(new Indicateur("Mois", Type.EVOLUTION, 1L, Regroupement.MOIS, null, null), FICHES));
    }

    @Test
    void un_indicateur_incoherent_est_refuse() {
        assertThrows(IllegalArgumentException.class, () -> new Indicateur(" ", Type.COMPTEUR, 1L, null, null, null));
        assertThrows(IllegalArgumentException.class, () -> new Indicateur("x".repeat(101), Type.COMPTEUR, 1L, null, null, null));
        assertThrows(IllegalArgumentException.class, () -> new Indicateur(null, Type.COMPTEUR, 1L, null, null, null));
        assertThrows(IllegalArgumentException.class, () -> new Indicateur("T", null, 1L, null, null, null));
        assertThrows(IllegalArgumentException.class, () -> new Indicateur("T", Type.COMPTEUR, null, null, null, null));
        assertThrows(IllegalArgumentException.class, () -> new Indicateur("T", Type.COMPTEUR, 1L, Regroupement.STATUT, null, null));
        assertThrows(IllegalArgumentException.class, () -> new Indicateur("T", Type.BARRES, 1L, Regroupement.CHAMP, " ", null));
        assertThrows(IllegalArgumentException.class, () -> new Indicateur("T", Type.BARRES, 1L, Regroupement.CHAMP, null, null));
        assertThrows(IllegalArgumentException.class, () -> new Indicateur("T", Type.BARRES, 1L, Regroupement.MOIS, null, null));
        assertThrows(IllegalArgumentException.class, () -> new Indicateur("T", Type.EVOLUTION, 1L, Regroupement.STATUT, null, null));
    }

    @Test
    void le_tableau_appartient_a_son_proprietaire_et_se_partage() {
        TableauBord t = new TableauBord(1L, "Alice@CHU.example", Instant.EPOCH);
        assertThrows(IllegalArgumentException.class, () -> t.configurer(" ", false, "[]", Instant.EPOCH));
        assertThrows(IllegalArgumentException.class, () -> t.configurer(null, false, "[]", Instant.EPOCH));
        assertThrows(IllegalArgumentException.class, () -> t.configurer("x".repeat(121), false, "[]", Instant.EPOCH));
        t.configurer(" Pilotage ", false, "[]", Instant.EPOCH.plusSeconds(1));
        assertEquals("Pilotage", t.getNom());
        assertTrue(t.estProprietaire("alice@chu.example"));
        assertFalse(t.estProprietaire(null));
        assertFalse(t.estVisiblePar("bob@chu.example"));
        t.configurer("Pilotage", true, "[]", Instant.EPOCH);
        assertTrue(t.estVisiblePar("bob@chu.example"));
        assertEquals(List.of(1L, "alice@chu.example", "[]", true), List.of(t.getTenantId(), t.getProprietaire(), t.getIndicateursJson(), t.isPartage()));
        assertEquals(Instant.EPOCH, t.getModifieLe());
    }

    @Test
    void le_rapport_compte_ses_lignes() {
        RapportFiches r = new RapportFiches("T", "F", List.of(), Map.of("SOUMIS", 2L), List.of("Fiche"), List.of(List.of("A"), List.of("B")));
        assertEquals(2, r.total());
        assertThrows(UnsupportedOperationException.class, () -> r.lignes().get(0).add("x"));
    }
}
