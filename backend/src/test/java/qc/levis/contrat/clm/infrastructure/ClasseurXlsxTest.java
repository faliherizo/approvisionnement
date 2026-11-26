package qc.levis.contrat.clm.infrastructure;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.List;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

/** Export Excel des rapports (Annexe 10, AF-37) : en-tête en gras, texte jamais évalué comme formule, panne d'écriture signalée. */
class ClasseurXlsxTest {

    @Test
    void chaque_cellule_est_du_texte_et_l_entete_est_en_gras() throws IOException {
        byte[] octets = new ClasseurXlsx().xlsx("Rapport : délais/IQ", List.of("Référence", "Titre"),
                List.of(Arrays.asList("TR-1", "=HYPERLINK(\"x\")"), Arrays.asList("TR-2", null)));
        try (XSSFWorkbook c = new XSSFWorkbook(new ByteArrayInputStream(octets))) {
            var s = c.getSheetAt(0);
            assertTrue(c.getFontAt(s.getRow(0).getCell(0).getCellStyle().getFontIndex()).getBold());
            assertEquals(CellType.STRING, s.getRow(1).getCell(1).getCellType(), "une formule reste une chaîne");
            assertEquals("=HYPERLINK(\"x\")", s.getRow(1).getCell(1).getStringCellValue());
            assertEquals("", s.getRow(2).getCell(1).getStringCellValue());
            assertFalse(s.getSheetName().contains("/"), "nom de feuille assaini");
        }
    }

    @Test
    void une_ecriture_interrompue_est_signalee() throws IOException {
        Workbook classeur = mock(Workbook.class, RETURNS_DEEP_STUBS);
        doThrow(new IOException("disque plein")).when(classeur).write(any());
        assertThrows(UncheckedIOException.class, () -> new ClasseurXlsx(() -> classeur).xlsx("R", List.of("A"), List.of(List.of("x"))));
    }
}
