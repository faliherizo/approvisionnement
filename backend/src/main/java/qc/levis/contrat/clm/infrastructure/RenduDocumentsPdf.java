package qc.levis.contrat.clm.infrastructure;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;
import qc.levis.contrat.clm.application.port.RenduDocuments;
import qc.levis.contrat.clm.domain.FormatDocument;
import qc.levis.contrat.clm.domain.Modele;

/** Rendu documentaire : extraction de texte (PDFBox, POI), génération (OpenPDF), apposition des signatures (PDFBox). */
@Component
public class RenduDocumentsPdf implements RenduDocuments {

    @Override
    public String texte(FormatDocument.Type type, byte[] contenu) {
        try {
            if (type == FormatDocument.Type.DOCX) {
                try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(contenu));
                     XWPFWordExtractor ex = new XWPFWordExtractor(doc)) {
                    return ex.getText();
                }
            }
            try (PDDocument doc = PDDocument.load(contenu)) {
                return new PDFTextStripper().getText(doc);
            }
        } catch (IOException | RuntimeException e) {
            throw new IllegalStateException("Aperçu non disponible : document illisible.", e);
        }
    }

    @Override
    public byte[] genererPdf(String titre, String texte) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.LETTER, 60, 60, 60, 60);
        PdfWriter.getInstance(doc, out);
        doc.open();
        doc.add(new Paragraph(titre == null ? "" : titre, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
        doc.add(new Paragraph(" "));
        Font corps = FontFactory.getFont(FontFactory.HELVETICA, 11);
        for (String ligne : (texte == null ? "" : texte).split("\n", -1)) {
            doc.add(new Paragraph(ligne.isEmpty() ? " " : ligne, corps));
        }
        doc.close();
        return out.toByteArray();
    }

    @Override
    public byte[] genererDocx(String titre, String texte) {
        try (XWPFDocument doc = new XWPFDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            org.apache.poi.xwpf.usermodel.XWPFRun t = doc.createParagraph().createRun();
            t.setBold(true);
            t.setFontSize(16);
            t.setText(titre == null ? "" : titre);
            for (String ligne : (texte == null ? "" : texte).split("\n", -1)) {
                doc.createParagraph().createRun().setText(ligne);
            }
            doc.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Génération Word impossible.", e);
        }
    }

    @Override
    public byte[] apposer(byte[] pdf, List<Apposition> appositions) {
        try (PDDocument doc = PDDocument.load(pdf); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            List<Apposition> sansZone = new ArrayList<>();
            for (Apposition a : appositions) {
                Modele.Position p = a.position();
                if (p == null || p.page() > doc.getNumberOfPages()) {
                    sansZone.add(a);
                } else {
                    dansZone(doc, doc.getPage(p.page() - 1), a);
                }
            }
            if (!sansZone.isEmpty()) {
                pageDeSignatures(doc, sansZone);
            }
            doc.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Apposition des signatures impossible.", e);
        }
    }

    private void dansZone(PDDocument doc, PDPage page, Apposition a) throws IOException {
        PDRectangle box = page.getMediaBox();
        Modele.Position p = a.position();
        float x = (float) (box.getWidth() * p.x() / 100);
        float yHaut = (float) (box.getHeight() * (1 - p.y() / 100));
        float largeur = (float) (box.getWidth() * p.largeur() / 100);
        float hauteur = (float) (box.getHeight() * p.hauteur() / 100);
        try (PDPageContentStream cs = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
            cs.setLineWidth(0.8f);
            cs.addRect(x, yHaut - hauteur, largeur, hauteur);
            cs.stroke();
            ecrire(cs, x + 4, yHaut - 12, 10, PDType1Font.HELVETICA_BOLD, "Signé : " + a.signataire());
            ecrire(cs, x + 4, yHaut - 24, 7, PDType1Font.HELVETICA, a.horodatage() + " · " + a.reference());
        }
    }

    private void pageDeSignatures(PDDocument doc, List<Apposition> appositions) throws IOException {
        PDPage page = new PDPage(PDRectangle.LETTER);
        doc.addPage(page);
        try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
            float y = page.getMediaBox().getHeight() - 72;
            ecrire(cs, 60, y, 14, PDType1Font.HELVETICA_BOLD, "Page de signatures électroniques");
            y -= 30;
            for (Apposition a : appositions) {
                ecrire(cs, 60, y, 11, PDType1Font.HELVETICA_BOLD, "Signé : " + a.signataire());
                ecrire(cs, 60, y - 14, 9, PDType1Font.HELVETICA, "Le " + a.horodatage() + " · Trousse " + a.reference());
                y -= 40;
            }
        }
    }

    private static void ecrire(PDPageContentStream cs, float x, float y, float taille, PDType1Font police, String texte) throws IOException {
        cs.beginText();
        cs.setFont(police, taille);
        cs.newLineAtOffset(x, y);
        cs.showText(winAnsi(texte));
        cs.endText();
    }

    /** Les polices standard PDF ne couvrent que WinAnsi : tout autre caractère est remplacé plutôt que de faire échouer l'apposition. */
    static String winAnsi(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : (s == null ? "" : s).toCharArray()) {
            boolean ok = c >= 0x20 && c <= 0x7E || c >= 0xA0 && c <= 0xFF || "€‚ƒ„…†‡ˆ‰Š‹ŒŽ‘’“”•–—˜™š›œžŸ".indexOf(c) >= 0;
            sb.append(ok ? c : '?');
        }
        return sb.toString();
    }
}
