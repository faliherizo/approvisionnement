package qc.levis.contrat.saisie.infrastructure.web;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PlacesControllerTest {

    @Test
    void une_cellule_csv_ne_peut_pas_executer_de_formule() {
        assertEquals("\"'=HYPERLINK(\"\"x\"\")\"", PlacesController.cellule("=HYPERLINK(\"x\")"));
        assertEquals("\"'+1\"", PlacesController.cellule("+1"));
        assertEquals("\"'-2\"", PlacesController.cellule("-2"));
        assertEquals("\"'@SUM\"", PlacesController.cellule("@SUM"));
        assertEquals("\"Atelier\"", PlacesController.cellule("Atelier"));
        assertEquals("\"3\"", PlacesController.cellule(3));
        assertEquals("", PlacesController.cellule(null));
        assertEquals("\"\"", PlacesController.cellule(""));
    }
}
