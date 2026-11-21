package qc.levis.contrat.soutien.infrastructure.web;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class BanqueHeuresControllerTest {
    @Test
    void une_cellule_du_releve_ne_peut_pas_executer_de_formule() {
        assertEquals("\"'=1+1\"", BanqueHeuresController.cellule("=1+1"));
        assertEquals("\"a \"\"b\"\"\"", BanqueHeuresController.cellule("a \"b\""));
        assertEquals("", BanqueHeuresController.cellule(null));
    }
}
