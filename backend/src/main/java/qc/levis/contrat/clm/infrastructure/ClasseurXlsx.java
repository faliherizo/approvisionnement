package qc.levis.contrat.clm.infrastructure;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.function.Supplier;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import qc.levis.contrat.clm.application.port.ClasseurTableur;

/**
 * Classeur XLSX par Apache POI. Chaque cellule est écrite comme texte : une valeur commençant par « = » reste une chaîne et
 * n'est jamais évaluée comme formule (injection de formule dans un tableur).
 */
@Component
public class ClasseurXlsx implements ClasseurTableur {

    private final Supplier<Workbook> fabrique;

    public ClasseurXlsx() {
        this(XSSFWorkbook::new);
    }

    /** Fabrique du classeur : POI en exploitation, un double en essai pour éprouver l'échec d'écriture. */
    ClasseurXlsx(Supplier<Workbook> fabrique) {
        this.fabrique = fabrique;
    }

    @Override
    public byte[] xlsx(String feuille, List<String> entetes, List<List<String>> lignes) {
        try (Workbook classeur = fabrique.get(); ByteArrayOutputStream sortie = new ByteArrayOutputStream()) {
            Sheet s = classeur.createSheet(WorkbookUtil.createSafeSheetName(feuille));
            CellStyle gras = classeur.createCellStyle();
            Font police = classeur.createFont();
            police.setBold(true);
            gras.setFont(police);
            ecrire(s.createRow(0), entetes, gras);
            for (int i = 0; i < lignes.size(); i++) {
                ecrire(s.createRow(i + 1), lignes.get(i), null);
            }
            s.createFreezePane(0, 1);
            classeur.write(sortie);
            return sortie.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void ecrire(Row r, List<String> valeurs, CellStyle style) {
        for (int j = 0; j < valeurs.size(); j++) {
            Cell c = r.createCell(j);
            c.setCellValue(valeurs.get(j) == null ? "" : valeurs.get(j));
            if (style != null) {
                c.setCellStyle(style);
            }
        }
    }
}
