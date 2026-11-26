package qc.levis.contrat.clm.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

/** Annexe 10 IQ, AF-46 (RST-49) : priorité de dossier, règle de dérivation et rappels accélérés. */
class PrioriteTrousseTest {

    private static final Instant T0 = Instant.parse("2026-11-26T08:00:00Z");

    @Test
    void la_priorite_se_deduit_du_niveau_de_risque() {
        assertEquals(PrioriteTrousse.BASSE, PrioriteTrousse.deduite(Identite.Risque.FAIBLE));
        assertEquals(PrioriteTrousse.NORMALE, PrioriteTrousse.deduite(Identite.Risque.MOYEN));
        assertEquals(PrioriteTrousse.HAUTE, PrioriteTrousse.deduite(Identite.Risque.ELEVE));
    }

    @Test
    void le_rang_ordonne_de_basse_a_critique() {
        assertTrue(PrioriteTrousse.BASSE.rang() < PrioriteTrousse.NORMALE.rang());
        assertTrue(PrioriteTrousse.NORMALE.rang() < PrioriteTrousse.HAUTE.rang());
        assertTrue(PrioriteTrousse.HAUTE.rang() < PrioriteTrousse.CRITIQUE.rang());
    }

    @Test
    void un_code_se_lit_sans_egard_a_la_casse_et_un_code_inconnu_est_refuse() {
        assertEquals(PrioriteTrousse.CRITIQUE, PrioriteTrousse.depuis(" critique "));
        PrioriteTrousse.PrioriteInconnueException e = assertThrows(PrioriteTrousse.PrioriteInconnueException.class,
                () -> PrioriteTrousse.depuis("URGENTE"));
        assertTrue(e.getMessage().contains("URGENTE"));
        assertThrows(IllegalArgumentException.class, () -> PrioriteTrousse.depuis(null));
    }

    @Test
    void les_priorites_elevees_rapprochent_les_rappels_sans_descendre_sous_six_heures() {
        assertEquals(Duration.ofDays(2), PrioriteTrousse.BASSE.intervalleRappel(2));
        assertEquals(Duration.ofDays(2), PrioriteTrousse.NORMALE.intervalleRappel(2));
        assertEquals(Duration.ofDays(1), PrioriteTrousse.HAUTE.intervalleRappel(2));
        assertEquals(Duration.ofHours(12), PrioriteTrousse.CRITIQUE.intervalleRappel(2));
        assertEquals(Duration.ofHours(6), PrioriteTrousse.CRITIQUE.intervalleRappel(1), "1 jour / 4 = 6 h, le plancher");
        assertEquals(PrioriteTrousse.INTERVALLE_MIN, PrioriteTrousse.HAUTE.intervalleRappel(0), "jamais sous le plancher");
    }

    @Test
    void un_rappel_critique_est_du_avant_un_rappel_normal() {
        Relances.Cadence normale = new Relances.Cadence(2, 3, PrioriteTrousse.NORMALE);
        Relances.Cadence critique = new Relances.Cadence(2, 3, PrioriteTrousse.CRITIQUE);
        Instant douzeHeures = T0.plus(Duration.ofHours(12));
        assertFalse(Relances.rappelDu(T0, null, 0, douzeHeures, normale));
        assertTrue(Relances.rappelDu(T0, null, 0, douzeHeures, critique));
        assertFalse(Relances.rappelDu(T0, douzeHeures, 1, douzeHeures.plus(Duration.ofHours(11)), critique), "depuis le dernier rappel");
        assertFalse(Relances.rappelDu(T0, null, 3, T0.plus(Duration.ofDays(9)), critique), "plafond atteint");
        assertFalse(Relances.rappelDu(T0, null, 0, T0.plus(Duration.ofDays(9)), new Relances.Cadence(0, 3, PrioriteTrousse.CRITIQUE)),
                "rappels désactivés");
        assertFalse(Relances.rappelDu(null, null, 0, douzeHeures, critique), "étape non activée");
    }

    @Test
    void la_trousse_suit_son_risque_tant_qu_aucune_priorite_n_est_choisie() {
        Trousse t = new Trousse(1L, "TR-1", "Titre", "init@iq.example", T0);
        assertEquals(PrioriteTrousse.NORMALE, t.getPriorite());
        assertFalse(t.isPrioriteChoisie());
        t.setRisque(Identite.Risque.ELEVE);
        assertEquals(PrioriteTrousse.HAUTE, t.getPriorite());

        t.prioriser(PrioriteTrousse.CRITIQUE);
        assertTrue(t.isPrioriteChoisie());
        t.setRisque(Identite.Risque.FAIBLE);
        assertEquals(PrioriteTrousse.CRITIQUE, t.getPriorite(), "une priorité choisie ne suit plus le risque");

        t.prioriser(null);
        assertFalse(t.isPrioriteChoisie());
        assertEquals(PrioriteTrousse.BASSE, t.getPriorite(), "retour à la priorité déduite du risque");
    }

    @Test
    void la_cadence_de_la_trousse_porte_sa_frequence_son_plafond_et_sa_priorite() {
        Trousse t = new Trousse(1L, "TR-2", "Titre", "init@iq.example", T0);
        t.setRappelFrequenceJours(4);
        t.setRappelsMax(5);
        t.prioriser(PrioriteTrousse.HAUTE);
        assertEquals(new Relances.Cadence(4, 5, PrioriteTrousse.HAUTE), t.cadenceRappels());
        assertEquals(Duration.ofDays(2), t.cadenceRappels().intervalle());
    }
}
