package qc.levis.contrat.redaction.infrastructure;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import org.springframework.stereotype.Component;
import qc.levis.contrat.redaction.domain.CorpsGabarit;
import qc.levis.contrat.redaction.domain.Gabarit;
import qc.levis.contrat.soumission.domain.Bordereau;

/** Adaptateur PDF (OpenPDF). Applique le modèle de document du tenant :
 *  police, couleurs (titres, accents), logo, marque, pied de page, page de couverture. */
@Component
public class PdfContractRenderer implements DocumentRenderer {

    @Override
    public Format format() { return Format.PDF; }

    @Override
    public byte[] rendre(ContractModel m, Branding branding) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            GabaritRendu g = m.gabarit();
            com.lowagie.text.Document pdf = new com.lowagie.text.Document(PageSize.LETTER,
                    g.margeGauche(), g.margeDroite(), g.margeHaut(), g.margeBas());
            PdfWriter writer = PdfWriter.getInstance(pdf, out);
            writer.setPageEvent(new PiedDePage(m.titre(), m.gabarit().piedLabel()));
            pdf.open();
            new Pass(pdf, branding).ecrire(m);
            pdf.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Échec du rendu PDF: " + e.getMessage(), e);
        }
    }

    /** Rendu isolé (état par appel, thread-safe). */
    private static final class Pass {
        private static final Color BLEU = new Color(11, 74, 138);

        private final com.lowagie.text.Document pdf;
        private final Branding b;
        private final Color titre;
        private final Color accent;
        private final int famille;

        Pass(com.lowagie.text.Document pdf, Branding b) {
            this.pdf = pdf;
            this.b = b;
            this.titre = couleur(b.couleurTitreHex());
            this.accent = couleur(b.couleurAccentHex());
            this.famille = famille(b.police());
        }

        /** Couleur hexadécimale, repli sur le bleu plateforme. */
        private static Color couleur(String hex) {
            try {
                return Color.decode(hex);
            } catch (RuntimeException e) {
                return BLEU;
            }
        }

        /** Famille de police OpenPDF selon le modèle. */
        private static int famille(String police) {
            if ("TIMES".equalsIgnoreCase(police)) return Font.TIMES_ROMAN;
            if ("COURIER".equalsIgnoreCase(police)) return Font.COURIER;
            return Font.HELVETICA;
        }

        void ecrire(ContractModel m) throws DocumentException {
            if (b.afficherCouverture()) {
                pageCouverture(m);
                pdf.newPage();
            }
            if (m.gabarit().corps() == CorpsGabarit.BORDEREAU) {
                bordereauSeul(m);
                return;
            }
            tableDesMatieres(m);
            pdf.newPage();
            preambule(m);
            corps(m);
            bordereau(m);
            annexes(m);
        }

        private Font font(int taille, int style, Color couleur) {
            return new Font(famille, taille, style, couleur);
        }

        private void pageCouverture(ContractModel m) throws DocumentException {
            GabaritRendu g = m.gabarit();
            for (int i = 0; i < 3; i++) pdf.add(Chunk.NEWLINE);
            ajouterLogo();
            pdf.add(centre(g.surLabel(), 12, Font.NORMAL, Color.GRAY));
            pdf.add(centre(g.label(), 26, Font.BOLD, titre));
            if (notBlank(m.numeroAO())) pdf.add(centre("NO " + m.numeroAO(), 13, Font.BOLD, Color.DARK_GRAY));
            if (g.aSousTitre()) pdf.add(centre("(" + g.sousTitre() + ")", 11, Font.ITALIC, Color.DARK_GRAY));
            pdf.add(Chunk.NEWLINE);
            pdf.add(centre(m.titre(), 16, Font.BOLD, Color.BLACK));
            for (int i = 0; i < 8; i++) pdf.add(Chunk.NEWLINE);
            pdf.add(centre("Émis le " + m.date(), 10, Font.ITALIC, Color.GRAY));
            pdf.add(centre("Propulsé par " + b.marque(), 9, Font.ITALIC, Color.GRAY));
        }

        private void ajouterLogo() {
            if (!b.aLogoImage()) return;
            try {
                String data = b.logoImage();
                byte[] bytes = Base64.getDecoder().decode(data.substring(data.indexOf(',') + 1));
                Image logo = Image.getInstance(bytes);
                logo.scaleToFit(170, 74);
                logo.setAlignment(Element.ALIGN_CENTER);
                pdf.add(logo);
                pdf.add(Chunk.NEWLINE);
            } catch (Exception e) {
                // Logo illisible : la marque textuelle prend le relais.
            }
        }

        private void tableDesMatieres(ContractModel m) throws DocumentException {
            pdf.add(titreSection("TABLE DES MATIÈRES"));
            pdf.add(Chunk.NEWLINE);
            Font sec = font(11, Font.BOLD, Color.BLACK);
            Font f = font(11, Font.NORMAL, Color.BLACK);
            if (!m.preambuleVide()) pdf.add(new Paragraph("PRÉAMBULE", sec));
            for (ContractModel.Section s : m.sections()) {
                pdf.add(new Paragraph(s.titre(), sec));
                for (ContractModel.Article a : s.articles()) {
                    pdf.add(new Paragraph("   " + a.numero() + "  " + a.titre(), f));
                }
            }
            if (m.aBordereau()) pdf.add(new Paragraph(m.bordereau().titre(), sec));
            for (ContractModel.Annexe an : m.annexes()) {
                pdf.add(new Paragraph(an.numero() + "  " + an.objet(), sec));
            }
        }

        private void preambule(ContractModel m) throws DocumentException {
            if (m.preambuleVide()) return;
            pdf.add(titreSection("PRÉAMBULE"));
            Font normal = font(10, Font.NORMAL, Color.BLACK);
            Font gras = font(10, Font.BOLD, Color.BLACK);
            if (notBlank(m.organismePublic())) {
                pdf.add(new Paragraph("ENTRE :", gras));
                pdf.add(new Paragraph(m.organismePublic(), normal));
                pdf.add(new Paragraph("ci-après désignée l'« ORGANISME PUBLIC »;", normal));
                pdf.add(Chunk.NEWLINE);
            }
            if (notBlank(m.prestataire())) {
                pdf.add(new Paragraph("ET :", gras));
                pdf.add(new Paragraph(m.prestataire(), normal));
                pdf.add(new Paragraph("ci-après désigné le « PRESTATAIRE DE SERVICES »;", normal));
                pdf.add(Chunk.NEWLINE);
            }
            if (notBlank(m.contexte())) pdf.add(new Paragraph("Contexte : " + m.contexte(), normal));
            pdf.add(Chunk.NEWLINE);
        }

        private void corps(ContractModel m) throws DocumentException {
            Font titreSec = font(13, Font.BOLD, titre);
            Font titreArt = font(11, Font.BOLD, Color.DARK_GRAY);
            Font corps = font(10, Font.NORMAL, Color.BLACK);
            for (ContractModel.Section s : m.sections()) {
                Paragraph hs = new Paragraph(s.titre().toUpperCase(), titreSec);
                hs.setSpacingBefore(14);
                hs.setSpacingAfter(4);
                pdf.add(hs);
                for (ContractModel.Article a : s.articles()) {
                    Paragraph h = new Paragraph(a.numero() + "  " + a.titre(), titreArt);
                    h.setSpacingBefore(8);
                    pdf.add(h);
                    pdf.add(new Paragraph(a.texte(), corps));
                }
            }
        }

        private void bordereau(ContractModel m) throws DocumentException {
            if (!m.aBordereau()) return;
            pdf.newPage();
            pdf.add(titreSection(m.bordereau().titre().toUpperCase()));
            PdfPTable t = new PdfPTable(new float[]{5, 1.4f, 1.4f, 1.6f});
            t.setWidthPercentage(100);
            t.setSpacingBefore(8);
            entete(t, "Description", "Quantité", "Taux", "Total");
            Font cell = font(9, Font.NORMAL, Color.BLACK);
            for (ContractModel.Bordereau.Ligne l : m.bordereau().lignes()) {
                t.addCell(new Phrase(l.description(), cell));
                t.addCell(alignDroite(l.quantite(), cell));
                t.addCell(alignDroite(l.taux(), cell));
                t.addCell(alignDroite(l.total(), cell));
            }
            PdfPCell totalLabel = new PdfPCell(new Phrase("TOTAL", font(9, Font.BOLD, Color.BLACK)));
            totalLabel.setColspan(3);
            totalLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
            t.addCell(totalLabel);
            t.addCell(alignDroite(m.bordereau().total(), font(9, Font.BOLD, Color.BLACK)));
            pdf.add(t);
        }

        /** Gabarit « Bordereau de prix » autonome : grille aux colonnes SCT + bloc signature. */
        private void bordereauSeul(ContractModel m) throws DocumentException {
            String titreBordereau = m.aBordereau() ? m.bordereau().titre() : "Bordereau de prix";
            pdf.add(titreSection(titreBordereau.toUpperCase()));
            if (notBlank(m.contexte())) {
                pdf.add(new Paragraph(m.contexte(), font(10, Font.NORMAL, Color.BLACK)));
            }
            PdfPTable t = new PdfPTable(new float[]{4.6f, 1.6f, 1.4f, 1.5f, 1.2f, 1.7f});
            t.setWidthPercentage(100);
            t.setSpacingBefore(10);
            entete(t, "Description", "Heures estimées annuelles", "Nombre de ressources",
                    "Taux horaire", "Nombre d'années", "Total");
            Font cell = font(9, Font.NORMAL, Color.BLACK);
            if (m.aBordereau()) {
                for (ContractModel.Bordereau.Ligne l : m.bordereau().lignes()) {
                    t.addCell(new Phrase(l.description(), cell));
                    t.addCell(alignDroite(l.quantite(), cell));
                    t.addCell(alignDroite("", cell));
                    t.addCell(alignDroite(l.taux(), cell));
                    t.addCell(alignDroite("", cell));
                    t.addCell(alignDroite(l.total(), cell));
                }
            }
            PdfPCell grandTotalLabel = new PdfPCell(new Phrase("GRAND TOTAL DE LA SOUMISSION", font(9, Font.BOLD, Color.BLACK)));
            grandTotalLabel.setColspan(5);
            grandTotalLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
            t.addCell(grandTotalLabel);
            t.addCell(alignDroite(m.aBordereau() ? m.bordereau().total() : "", font(9, Font.BOLD, Color.BLACK)));
            pdf.add(t);
            blocSignature();
        }

        /** Bloc de signature du soumissionnaire (identique aux bordereaux SCT). */
        private void blocSignature() throws DocumentException {
            for (int i = 0; i < 3; i++) pdf.add(Chunk.NEWLINE);
            pdf.add(titreSection("SIGNATURE DU SOUMISSIONNAIRE"));
            Font f = font(10, Font.NORMAL, Color.BLACK);
            pdf.add(new Paragraph("Nom du soumissionnaire : ______________________________________", f));
            pdf.add(Chunk.NEWLINE);
            pdf.add(new Paragraph("Signature : ______________________________________", f));
            pdf.add(new Paragraph("(Nom et fonction du signataire autorisé, en lettres moulées)",
                    font(9, Font.ITALIC, Color.GRAY)));
            pdf.add(Chunk.NEWLINE);
            pdf.add(new Paragraph("Date : ______________________________________", f));
        }

        private void entete(PdfPTable t, String... cols) {
            Font f = font(9, Font.BOLD, Color.WHITE);
            for (String c : cols) {
                PdfPCell cell = new PdfPCell(new Phrase(c, f));
                cell.setBackgroundColor(accent);
                cell.setPadding(4);
                t.addCell(cell);
            }
        }

        private PdfPCell alignDroite(String texte, Font f) {
            PdfPCell c = new PdfPCell(new Phrase(texte, f));
            c.setHorizontalAlignment(Element.ALIGN_RIGHT);
            return c;
        }

        private void annexes(ContractModel m) throws DocumentException {
            if (m.annexes().isEmpty()) return;
            pdf.newPage();
            pdf.add(titreSection("LISTE DES ANNEXES"));
            Font titreAnnexe = font(12, Font.BOLD, titre);
            Font corps = font(10, Font.NORMAL, Color.BLACK);
            for (ContractModel.Annexe an : m.annexes()) {
                Paragraph h = new Paragraph(an.numero() + " — " + an.objet(), titreAnnexe);
                h.setSpacingBefore(10);
                pdf.add(h);
                an.elements().forEach(e -> pdf.add(new Paragraph("• " + e, corps)));
            }
        }

        private Paragraph titreSection(String texte) {
            Paragraph p = new Paragraph(texte, font(15, Font.BOLD, titre));
            p.setSpacingAfter(6);
            return p;
        }

        private Paragraph centre(String texte, int taille, int style, Color couleur) {
            Paragraph p = new Paragraph(texte, font(taille, style, couleur));
            p.setAlignment(Element.ALIGN_CENTER);
            return p;
        }

        private boolean notBlank(String s) { return s != null && !s.isBlank(); }
    }

    /** Pied de page répété : titre courant + « Page X de N » + pied personnalisé (ou marque). */
    private static class PiedDePage extends PdfPageEventHelper {
        private final String titre;
        private final String coin;
        private PdfTemplate total;
        private BaseFont bf;

        PiedDePage(String titre, String coin) {
            this.titre = titre;
            this.coin = (coin != null && !coin.isBlank()) ? coin : "";
        }

        @Override
        public void onOpenDocument(PdfWriter writer, com.lowagie.text.Document document) {
            try { bf = BaseFont.createFont(); } catch (Exception e) { throw new IllegalStateException(e); }
            total = writer.getDirectContent().createTemplate(30, 12);
        }

        @Override
        public void onEndPage(PdfWriter writer, com.lowagie.text.Document document) {
            PdfContentByte cb = writer.getDirectContent();
            Font f = new Font(bf, 8, Font.NORMAL, Color.GRAY);
            float y = document.bottom() - 18;
            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                    new Phrase(tronquer(titre), f), document.left(), y, 0);
            String prefixe = "Page " + writer.getPageNumber() + " de ";
            float largeur = bf.getWidthPoint(prefixe, 8);
            float centre = (document.right() + document.left()) / 2;
            float x = centre - largeur / 2;
            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, new Phrase(prefixe, f), x, y, 0);
            cb.addTemplate(total, x + largeur, y);
            ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT,
                    new Phrase(tronquer(coin), f), document.right(), y, 0);
        }

        @Override
        public void onCloseDocument(PdfWriter writer, com.lowagie.text.Document document) {
            total.beginText();
            total.setFontAndSize(bf, 8);
            total.setColorFill(Color.GRAY);
            total.showText(String.valueOf(writer.getPageNumber() - 1));
            total.endText();
        }

        private String tronquer(String s) {
            return s == null ? "" : (s.length() > 70 ? s.substring(0, 70) + "…" : s);
        }
    }
}
