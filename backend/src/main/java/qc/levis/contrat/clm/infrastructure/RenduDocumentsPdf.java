package qc.levis.contrat.clm.infrastructure;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Clock;
import java.util.Calendar;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;
import qc.levis.contrat.clm.application.port.RenduDocuments;
import qc.levis.contrat.clm.domain.FormatDocument;
import qc.levis.contrat.clm.domain.Modele;

/** Rendu documentaire : extraction de texte (PDFBox, POI), génération PDF/A-2b et apposition des signatures (PDFBox). */
@Component
public class RenduDocumentsPdf implements RenduDocuments {

    private static final float MARGE = 60;
    private static final float LARGEUR_UTILE = PDRectangle.LETTER.getWidth() - 2 * MARGE;
    private static final float TAILLE_TITRE = 16;
    private static final float TAILLE_CORPS = 11;
    private static final float INTERLIGNE = 1.35f;

    private final Clock horloge;

    public RenduDocumentsPdf(Clock horloge) {
        this.horloge = horloge;
    }

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

    /** PDF/A-2b : police intégrée, métadonnées et profil de couleur (addenda 4, Q14). */
    @Override
    public byte[] genererPdf(String titre, String texte) {
        return produire("Génération PDF impossible.", out -> {
            try (PDDocument doc = new PDDocument()) {
                PDFont police = ConformitePdfA.police(doc);
                Mise mise = new Mise(doc, police);
                mise.paragraphe(titre == null ? "" : titre, TAILLE_TITRE);
                mise.paragraphe("", TAILLE_CORPS);
                for (String ligne : (texte == null ? "" : texte).split("\n", -1)) {
                    mise.paragraphe(ligne, TAILLE_CORPS);
                }
                mise.fermer();
                ConformitePdfA.declarer(doc, titre == null ? "" : titre, maintenant());
                doc.save(out);
            }
        });
    }

    /** Production en mémoire : les échecs d'écriture, de police ou de métadonnées deviennent une erreur explicite. */
    @FunctionalInterface
    private interface Production {
        void dans(ByteArrayOutputStream out) throws Exception;
    }

    private static byte[] produire(String echec, Production production) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            production.dans(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException(echec, e);
        }
    }

    private Calendar maintenant() {
        Calendar le = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"));
        le.setTimeInMillis(horloge.millis());
        return le;
    }

    /** Mise en page lettre US, marges de 60 points, retour à la ligne au mot et saut de page. */
    private static final class Mise {
        private final PDDocument doc;
        private final PDFont police;
        private PDPageContentStream cs;
        private float y;

        Mise(PDDocument doc, PDFont police) {
            this.doc = doc;
            this.police = police;
        }

        void paragraphe(String texte, float taille) throws IOException {
            for (String ligne : lignes(ConformitePdfA.affichable(police, texte), taille)) {
                if (cs == null || y < MARGE + taille) {
                    nouvellePage();
                }
                y -= taille * INTERLIGNE;
                ecrire(cs, MARGE, y, taille, police, ligne);
            }
        }

        private List<String> lignes(String texte, float taille) throws IOException {
            List<String> lignes = new ArrayList<>();
            StringBuilder courante = new StringBuilder();
            for (String mot : texte.split(" ", -1)) {
                String essai = courante.isEmpty() ? mot : courante + " " + mot;
                if (!courante.isEmpty() && police.getStringWidth(essai) / 1000 * taille > LARGEUR_UTILE) {
                    lignes.add(courante.toString());
                    courante = new StringBuilder(mot);
                } else {
                    courante = new StringBuilder(essai);
                }
            }
            lignes.add(courante.toString());
            return lignes;
        }

        private void nouvellePage() throws IOException {
            fermer();
            PDPage page = new PDPage(PDRectangle.LETTER);
            doc.addPage(page);
            cs = new PDPageContentStream(doc, page);
            y = PDRectangle.LETTER.getHeight() - MARGE;
        }

        void fermer() throws IOException {
            if (cs != null) {
                cs.close();
            }
        }
    }

    @Override
    public byte[] fusionnerDocx(byte[] gabarit, Map<String, Object> valeurs, List<ClauseFusionnee> clauses) {
        return FusionDocx.fusionner(gabarit, valeurs, clauses);
    }

    @Override
    public byte[] genererDocx(String titre, String texte) {
        return produire("Génération Word impossible.", out -> {
            try (XWPFDocument doc = new XWPFDocument()) {
                org.apache.poi.xwpf.usermodel.XWPFRun t = doc.createParagraph().createRun();
                t.setBold(true);
                t.setFontSize(16);
                t.setText(titre == null ? "" : titre);
                for (String ligne : (texte == null ? "" : texte).split("\n", -1)) {
                    doc.createParagraph().createRun().setText(ligne);
                }
                doc.write(out);
            }
        });
    }

    @Override
    public byte[] apposer(byte[] pdf, List<Apposition> appositions) {
        try (PDDocument doc = PDDocument.load(pdf); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDFont police = ConformitePdfA.police(doc);
            List<Apposition> sansZone = new ArrayList<>();
            for (Apposition a : appositions) {
                Modele.Position p = a.position();
                if (p == null || p.page() > doc.getNumberOfPages()) {
                    sansZone.add(a);
                } else {
                    dansZone(doc, doc.getPage(p.page() - 1), a, police);
                }
            }
            if (!sansZone.isEmpty()) {
                pageDeSignatures(doc, sansZone, police);
            }
            doc.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Apposition des signatures impossible.", e);
        }
    }

    private void dansZone(PDDocument doc, PDPage page, Apposition a, PDFont police) throws IOException {
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
            ecrire(cs, x + 4, yHaut - 12, 10, police, "Signé : " + a.signataire());
            ecrire(cs, x + 4, yHaut - 24, 7, police, a.horodatage() + " · " + a.reference());
        }
    }

    private void pageDeSignatures(PDDocument doc, List<Apposition> appositions, PDFont police) throws IOException {
        PDPage page = new PDPage(PDRectangle.LETTER);
        doc.addPage(page);
        try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
            float y = page.getMediaBox().getHeight() - 72;
            ecrire(cs, 60, y, 14, police, "Page de signatures électroniques");
            y -= 30;
            for (Apposition a : appositions) {
                ecrire(cs, 60, y, 11, police, "Signé : " + a.signataire());
                ecrire(cs, 60, y - 14, 9, police, "Le " + a.horodatage() + " · Trousse " + a.reference());
                y -= 40;
            }
        }
    }

    /** Police intégrée (PDF/A) : un document déjà conforme le reste après l'apposition des signatures. */
    private static void ecrire(PDPageContentStream cs, float x, float y, float taille, PDFont police, String texte) throws IOException {
        cs.beginText();
        cs.setFont(police, taille);
        cs.newLineAtOffset(x, y);
        cs.showText(ConformitePdfA.affichable(police, texte));
        cs.endText();
    }
}
