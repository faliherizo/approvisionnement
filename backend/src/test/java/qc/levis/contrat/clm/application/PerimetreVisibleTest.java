package qc.levis.contrat.clm.application;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Motif LIKE des affectations : la valeur telle que la sérialise JSON, entre guillemets, jokers de LIKE neutralisés. */
class PerimetreVisibleTest {

    @Test
    void la_valeur_est_citee_entre_guillemets_telle_quelle() {
        assertEquals("%\"Lea.Martin@IQ.example\"%", PerimetreVisible.motif("Lea.Martin@IQ.example"));
    }

    @Test
    void les_jokers_de_like_sont_neutralises() {
        assertEquals("%\"FINANCE\\_PME 100\\%\"%", PerimetreVisible.motif("FINANCE_PME 100%"));
    }

    @Test
    void les_echappements_json_sont_reproduits_puis_proteges() {
        // JSON écrit a\"b et c\\d ; LIKE exige à son tour de doubler la barre oblique inverse.
        assertEquals("%\"a\\\\\"b c\\\\\\\\d\"%", PerimetreVisible.motif("a\"b c\\d"));
    }
}
