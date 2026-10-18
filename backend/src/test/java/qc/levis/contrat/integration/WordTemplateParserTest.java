package qc.levis.contrat.integration;

import java.io.ByteArrayOutputStream;
import java.util.List;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.redaction.infrastructure.WordTemplateParser.SectionWord;
import qc.levis.contrat.redaction.infrastructure.WordTemplateParser;
import static org.junit.jupiter.api.Assertions.*;

/** Tests unitaires du parseur de modèle Word (Apache POI). */
class WordTemplateParserTest {

    private final WordTemplateParser parser = new WordTemplateParser();

    private void heading(XWPFDocument doc, String texte) {
        XWPFParagraph p = doc.createParagraph();
        p.setStyle("Heading1");
        p.createRun().setText(texte);
    }

    private void grasSeul(XWPFDocument doc, String texte) {
        XWPFParagraph p = doc.createParagraph();
        XWPFRun r = p.createRun();
        r.setBold(true);
        r.setText(texte);
    }

    private void corps(XWPFDocument doc, String texte) {
        doc.createParagraph().createRun().setText(texte);
    }

    private byte[] octets(XWPFDocument doc) throws Exception {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            doc.write(out);
            doc.close();
            return out.toByteArray();
        }
    }

    @Test
    void extrait_titresParStyleEtParGras_avecCorps() throws Exception {
        byte[] docx;
        try (XWPFDocument doc = new XWPFDocument()) {
            corps(doc, "Préambule sans titre.");           // section sans titre (titre null)
            heading(doc, "Objet du contrat");               // titre par style
            corps(doc, "Le présent contrat a pour objet…");
            corps(doc, "Deuxième paragraphe de l'objet.");
            grasSeul(doc, "GARANTIE");                       // titre par gras seul
            corps(doc, "Garantie de 12 mois.");
            docx = octets(doc);
        }

        List<SectionWord> sections = parser.extraire(docx);

        assertEquals(3, sections.size());
        assertNull(sections.get(0).titre());
        assertEquals("Préambule sans titre.", sections.get(0).corps());
        assertEquals("Objet du contrat", sections.get(1).titre());
        assertTrue(sections.get(1).corps().contains("Le présent contrat"));
        assertTrue(sections.get(1).corps().contains("Deuxième paragraphe"));
        assertEquals("GARANTIE", sections.get(2).titre());
        assertEquals("Garantie de 12 mois.", sections.get(2).corps());
    }

    @Test
    void detection_titreFrancais_ligneLongue_etGrasMixte() throws Exception {
        byte[] docx;
        try (XWPFDocument doc = new XWPFDocument()) {
            var t = doc.createParagraph(); t.setStyle("Titre 1");   // style FR → titre
            // titre avec un run vide + un run gras (couvre le saut des runs vides)
            t.createRun().setText("");
            var r = t.createRun(); r.setBold(true); r.setText("Conditions générales");
            // ligne longue (>90 car.) non grasse → corps, pas un titre
            doc.createParagraph().createRun().setText(
                    "Ceci est un très long paragraphe de corps qui dépasse largement quatre-vingt-dix caractères au total.");
            // ligne courte avec gras + non-gras mélangés → PAS un titre (donc corps)
            var mix = doc.createParagraph();
            var b = mix.createRun(); b.setBold(true); b.setText("Partie ");
            var n = mix.createRun(); n.setBold(false); n.setText("normale");
            docx = octets(doc);
        }

        List<SectionWord> sections = parser.extraire(docx);
        assertEquals(1, sections.size());
        assertEquals("Conditions générales", sections.get(0).titre());
        assertTrue(sections.get(0).corps().contains("très long paragraphe"));
        assertTrue(sections.get(0).corps().contains("Partie normale"));
    }

    @Test
    void documentVide_donneAucuneSection() throws Exception {
        byte[] docx;
        try (XWPFDocument doc = new XWPFDocument()) {
            docx = octets(doc);
        }
        assertTrue(parser.extraire(docx).isEmpty());
    }

    @Test
    void octetsInvalides_leventUneErreurClaire() {
        assertThrows(IllegalArgumentException.class,
                () -> parser.extraire("ceci n'est pas un docx".getBytes()));
    }
}
