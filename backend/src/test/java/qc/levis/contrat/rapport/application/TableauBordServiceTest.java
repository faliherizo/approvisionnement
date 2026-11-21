package qc.levis.contrat.rapport.application;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TableauBordServiceTest {
    @Test
    void des_indicateurs_illisibles_se_lisent_comme_aucun() {
        TableauBordService s = new TableauBordService(null, null, null, java.time.Clock.systemUTC());
        assertTrue(s.lire("{pas du json").isEmpty());
        assertEquals(1, s.lire("[{\"titre\":\"T\",\"type\":\"COMPTEUR\",\"formulaireId\":1}]").size());
    }
}
