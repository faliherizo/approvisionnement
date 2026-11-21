package qc.levis.contrat.commun.infrastructure.pdf;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.xmpbox.xml.DomXmpParser;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.commun.infrastructure.pdf.DocumentPdfA.*;

class DocumentPdfATest {

    static final Calendar LE = new GregorianCalendar(2026, Calendar.NOVEMBER, 20);

    @Test
    void les_blocs_s_ecrivent_dans_l_ordre_en_pdf_a_2b() throws Exception {
        byte[] pdf = DocumentPdfA.produire("Rapport — Québec", List.of(new Titre("Section"), new Paragraphe("Ligne 1\nLigne 2"),
                new Paire("Nom", "Alice"), new Tableau(List.of("Fiche", "Statut"), List.of(List.of("RH-1", "SOUMIS"), java.util.Arrays.asList("RH-2", null)))), LE);
        try (PDDocument doc = PDDocument.load(pdf)) {
            String texte = new PDFTextStripper().getText(doc);
            for (String attendu : List.of("Rapport — Québec", "Section", "Ligne 1", "Ligne 2", "Nom", "Alice", "RH-1", "SOUMIS", "RH-2")) {
                assertTrue(texte.contains(attendu), attendu + " dans " + texte);
            }
            assertEquals(2, new DomXmpParser().parse(doc.getDocumentCatalog().getMetadata().exportXMPMetadata()).getPDFIdentificationSchema().getPart());
        }
    }

    @Test
    void un_long_contenu_passe_a_la_ligne_et_change_de_page() throws Exception {
        List<Bloc> blocs = new ArrayList<>();
        blocs.add(new Paragraphe("x".repeat(400)));
        for (int i = 0; i < 80; i++) blocs.add(new Paire("Question " + i, "réponse assez longue pour occuper la colonne de droite " + i));
        try (PDDocument doc = PDDocument.load(DocumentPdfA.produire("Long", blocs, LE))) {
            assertTrue(doc.getNumberOfPages() > 1);
            String texte = new PDFTextStripper().getText(doc);
            assertTrue(texte.contains("Question 79"));
        }
    }

    @Test
    void un_tableau_rejette_aucune_valeur_et_est_immuable() {
        Tableau t = new Tableau(List.of("a"), List.of(java.util.Arrays.asList((String) null)));
        assertEquals("", t.lignes().get(0).get(0));
        assertThrows(UnsupportedOperationException.class, () -> t.colonnes().add("b"));
    }
}
