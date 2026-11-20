package qc.levis.contrat.clm.infrastructure;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import org.apache.poi.wp.usermodel.HeaderFooterType;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.clm.application.port.RenduDocuments.ClauseFusionnee;

/**
 * Fusion dans le gabarit Word d'origine (addenda 4, Q22) : variables découpées sur plusieurs runs, mise en forme du
 * premier run conservée, tableaux (imbriqués), en-têtes et pieds de page, marqueur de clauses dans le corps ou une cellule.
 */
class FusionDocxTest {

    private static final Map<String, Object> VALEURS = Map.of("beneficiaire", "Agri-Nord inc.", "montant", "250 000 $");
    private static final List<ClauseFusionnee> CLAUSES = List.of(new ClauseFusionnee("Garantie", "Hypothèque mobilière\nde premier rang"),
            new ClauseFusionnee(null, "Sans titre"), new ClauseFusionnee("  ", null));

    /** Gabarit tel que Word l'enregistre : la variable « beneficiaire » répartie sur trois runs, le premier en gras. */
    static byte[] gabarit(boolean marqueurDansLeCorps) throws Exception {
        try (XWPFDocument doc = new XWPFDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XWPFParagraph p = doc.createParagraph();
            p.setStyle("Titre1");
            XWPFRun r1 = p.createRun();
            r1.setBold(true);
            r1.setText("Convention avec {{bene");
            p.createRun().setText("ficia");
            p.createRun().setText("ire}} pour {{ montant }} et {{inconnue}}.");
            XWPFTable t = doc.createTable(1, 2);
            t.getRow(0).getCell(0).setText("Montant");
            t.getRow(0).getCell(1).setText("{{montant}}");
            XWPFTable imbrique = t.getRow(0).getCell(1).insertNewTbl(t.getRow(0).getCell(1).getParagraphArray(0).getCTP().newCursor());
            imbrique.createRow().createCell().setText("{{beneficiaire}}");
            var entete = doc.createHeader(HeaderFooterType.DEFAULT);
            entete.createParagraph().createRun().setText("Réf. {{beneficiaire}}");
            entete.createTable(1, 1).getRow(0).getCell(0).setText("Montant : {{montant}}");
            doc.createFooter(HeaderFooterType.DEFAULT).createParagraph().createRun().setText("{{clauses}}");
            if (marqueurDansLeCorps) {
                doc.createParagraph().createRun().setText(" {{ clauses }} ");
                XWPFTable annexe = doc.createTable(1, 1);
                annexe.getRow(0).getCell(0).setText("{{clauses}}");
            }
            XWPFRun multiple = doc.createParagraph().createRun();
            multiple.setText("Signé par {{benef");
            multiple.getCTR().addNewT().setStringValue("iciaire}} le jour même");
            doc.createParagraph().createRun().setText("Fin du document.");
            doc.write(out);
            return out.toByteArray();
        }
    }

    @Test
    void les_variables_reparties_sur_plusieurs_runs_sont_remplacees_en_gardant_la_mise_en_forme() throws Exception {
        try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(FusionDocx.fusionner(gabarit(true), VALEURS, CLAUSES)))) {
            XWPFParagraph p = doc.getParagraphs().get(0);
            assertEquals("Convention avec Agri-Nord inc. pour 250 000 $ et [inconnue].", p.getText());
            assertTrue(p.getRuns().get(0).isBold(), "mise en forme du premier run conservée");
            assertEquals("Titre1", p.getStyle(), "style du paragraphe conservé");
            String tableau = doc.getTables().get(0).getText();
            assertTrue(tableau.contains("250 000 $"), tableau);
            assertEquals("Agri-Nord inc.", doc.getTables().get(0).getRow(0).getCell(1).getTables().get(0).getText().strip(), "tableau imbriqué");
            assertTrue(doc.getHeaderList().get(0).getText().contains("Réf. Agri-Nord inc."));
            assertEquals("Montant : 250 000 $", doc.getHeaderList().get(0).getTables().get(0).getText().strip(), "tableau d'en-tête");
            assertTrue(corps(doc).contains("Signé par Agri-Nord inc. le jour même"), "run à plusieurs textes réécrit d'un bloc");
            assertEquals("", doc.getFooterList().get(0).getText().strip(), "marqueur de pied de page vidé");
            String corps = texte(doc);
            assertFalse(corps.contains("{{"), corps);
            assertTrue(corps.indexOf("Garantie\nHypothèque mobilière\nde premier rang\nSans titre") >= 0, corps);
            assertTrue(corps.indexOf("Sans titre") < corps.indexOf("Fin du document."), "clauses insérées au marqueur");
            assertTrue(doc.getTables().get(1).getText().contains("Hypothèque mobilière"), "marqueur dans une cellule");
            XWPFParagraph titreClause = doc.getParagraphs().stream().filter(x -> x.getText().equals("Garantie")).findFirst().orElseThrow();
            assertTrue(titreClause.getRuns().get(0).isBold());
        }
    }

    @Test
    void sans_marqueur_les_clauses_s_ajoutent_en_fin_de_document() throws Exception {
        try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(FusionDocx.fusionner(gabarit(false), VALEURS, CLAUSES)))) {
            String corps = texte(doc);
            assertTrue(corps.indexOf("Fin du document.") < corps.indexOf("Garantie"), corps);
        }
    }

    @Test
    void les_variables_du_gabarit_se_lisent_comme_word_les_affiche() throws Exception {
        assertEquals(List.of("beneficiaire", "montant", "inconnue", "clauses", "beneficiaire", "montant", "beneficiaire", "clauses", "beneficiaire", "montant", "clauses"),
                FusionDocx.variables(gabarit(true)));
        assertThrows(IllegalArgumentException.class, () -> FusionDocx.variables("pas un docx".getBytes()));
        assertThrows(IllegalStateException.class, () -> FusionDocx.fusionner("pas un docx".getBytes(), VALEURS, List.of()));
    }

    @Test
    void le_rendu_delegue_la_fusion() throws Exception {
        RenduDocumentsPdf rendu = new RenduDocumentsPdf(java.time.Clock.systemUTC());
        byte[] docx = rendu.fusionnerDocx(gabarit(true), VALEURS, List.of());
        assertTrue(rendu.texte(qc.levis.contrat.clm.domain.FormatDocument.Type.DOCX, docx).contains("Agri-Nord inc."));
    }

    private static String corps(XWPFDocument doc) {
        return texte(doc);
    }

    private static String texte(XWPFDocument doc) {
        StringBuilder sb = new StringBuilder();
        doc.getParagraphs().forEach(p -> sb.append(p.getText()).append('\n'));
        return sb.toString();
    }
}
