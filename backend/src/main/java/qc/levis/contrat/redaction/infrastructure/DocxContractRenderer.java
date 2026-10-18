package qc.levis.contrat.redaction.infrastructure;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Component;

/** Adaptateur DOCX (Apache POI) : page couverture, table des matières, articles numérotés.
 *  Applique le branding du tenant (couleur primaire, logo, marque). */
@Component
public class DocxContractRenderer implements DocumentRenderer {

    @Override
    public Format format() { return Format.DOCX; }

    @Override
    public byte[] rendre(ContractModel m, Branding branding) {
        try (XWPFDocument doc = new XWPFDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            new Pass(doc, branding).ecrire(m);
            doc.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Échec du rendu DOCX: " + e.getMessage(), e);
        }
    }

    /** Rendu isolé : état porté par l'appel (thread-safe même si l'adaptateur est un singleton). */
    private static final class Pass {
        private static final String COULEUR_DEFAUT = "0B4A8A";

        private final XWPFDocument doc;
        private final Branding branding;
        private final String couleurTitre;
        private final String couleurAccent;
        private final String police;

        Pass(XWPFDocument doc, Branding branding) {
            this.doc = doc;
            this.branding = branding;
            this.couleurTitre = hex(branding.couleurTitreHex());
            this.couleurAccent = hex(branding.couleurAccentHex());
            this.police = fontFamily(branding.police());
        }

        /** Couleur hexadécimale (6 chiffres, sans #), repli sur le bleu plateforme. */
        private static String hex(String couleurHex) {
            if (couleurHex == null) return COULEUR_DEFAUT;
            String h = couleurHex.startsWith("#") ? couleurHex.substring(1) : couleurHex;
            return h.matches("[0-9a-fA-F]{6}") ? h : COULEUR_DEFAUT;
        }

        /** Famille de police Word selon le modèle. */
        private static String fontFamily(String police) {
            if ("TIMES".equalsIgnoreCase(police)) return "Times New Roman";
            if ("COURIER".equalsIgnoreCase(police)) return "Courier New";
            return "Helvetica";
        }

        void ecrire(ContractModel m) {
            if (branding.afficherCouverture()) {
                pageCouverture(m);
                doc.createParagraph().setPageBreak(true);
            }
            tableDesMatieres(m);
            doc.createParagraph().setPageBreak(true);
            preambule(m);
            corps(m);
            bordereau(m);
            annexes(m);
            piedDePage();
        }

        /** Mention de pied de page personnalisée (ex. confidentialité), en fin de document. */
        private void piedDePage() {
            if (!branding.aPiedDePage()) return;
            XWPFParagraph p = doc.createParagraph();
            p.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun r = run(p, branding.piedDePage(), 8, false);
            r.setColor("808080");
        }

        private void pageCouverture(ContractModel m) {
            ajouterLogo();
            centre(m.type().replace('_', ' '), 12, false);
            centre("CONTRAT", 26, true);
            if (notBlank(m.numeroAO())) centre("N° " + m.numeroAO(), 13, true);
            centre(m.titre(), 16, true);
            centre("Émis le " + m.date() + " · " + branding.marque(), 9, false);
        }

        /** Logo image du tenant (data URL base64) ; ignoré silencieusement si absent ou invalide. */
        private void ajouterLogo() {
            if (!branding.aLogoImage()) return;
            try {
                String data = branding.logoImage();
                byte[] bytes = Base64.getDecoder().decode(data.substring(data.indexOf(',') + 1));
                int type = data.contains("image/png")
                        ? Document.PICTURE_TYPE_PNG : Document.PICTURE_TYPE_JPEG;
                XWPFParagraph p = doc.createParagraph();
                p.setAlignment(ParagraphAlignment.CENTER);
                try (ByteArrayInputStream in = new ByteArrayInputStream(bytes)) {
                    p.createRun().addPicture(in, type, "logo", Units.toEMU(150), Units.toEMU(60));
                }
            } catch (Exception e) {
                // Logo illisible : la marque textuelle prend le relais.
            }
        }

        private void tableDesMatieres(ContractModel m) {
            titre("TABLE DES MATIÈRES", 15);
            if (!m.preambuleVide()) para("PRÉAMBULE", 11, true);
            for (ContractModel.Section s : m.sections()) {
                para(s.titre(), 11, true);
                s.articles().forEach(a -> para("   " + a.numero() + "  " + a.titre(), 11, false));
            }
            if (m.aBordereau()) para(m.bordereau().titre(), 11, true);
            m.annexes().forEach(an -> para(an.numero() + "  " + an.objet(), 11, true));
        }

        private void preambule(ContractModel m) {
            if (m.preambuleVide()) return;
            titre("PRÉAMBULE", 15);
            if (notBlank(m.organismePublic())) {
                para("ENTRE :", 10, true);
                para(m.organismePublic(), 10, false);
                para("ci-après « l'ORGANISME PUBLIC »;", 10, false);
            }
            if (notBlank(m.prestataire())) {
                para("ET :", 10, true);
                para(m.prestataire(), 10, false);
                para("ci-après « le PRESTATAIRE »;", 10, false);
            }
            if (notBlank(m.contexte())) para("Contexte : " + m.contexte(), 10, false);
        }

        private void corps(ContractModel m) {
            for (ContractModel.Section s : m.sections()) {
                titre(s.titre().toUpperCase(), 13);
                for (ContractModel.Article a : s.articles()) {
                    titre(a.numero() + "  " + a.titre(), 12);
                    para(a.texte(), 11, false);
                }
            }
        }

        private void bordereau(ContractModel m) {
            if (!m.aBordereau()) return;
            doc.createParagraph().setPageBreak(true);
            titre(m.bordereau().titre().toUpperCase(), 15);
            XWPFTable t = doc.createTable();
            enteteLigne(t.getRow(0), "Description", "Quantité", "Taux", "Total");
            for (ContractModel.Bordereau.Ligne l : m.bordereau().lignes()) {
                cellules(t.createRow(), l.description(), l.quantite(), l.taux(), l.total());
            }
            cellules(t.createRow(), "TOTAL", "", "", m.bordereau().total());
        }

        private void enteteLigne(XWPFTableRow row, String... cols) {
            for (int i = 0; i < cols.length; i++) {
                XWPFTableCell c = i == 0 ? row.getCell(0) : row.addNewTableCell();
                run(c.getParagraphs().get(0), cols[i], 10, true).setColor(couleurAccent);
            }
        }

        private void cellules(XWPFTableRow row, String... valeurs) {
            for (int i = 0; i < valeurs.length; i++) {
                XWPFTableCell c = i == 0 ? row.getCell(0) : row.addNewTableCell();
                run(c.getParagraphs().get(0), valeurs[i], 10, false);
            }
        }

        private void annexes(ContractModel m) {
            if (m.annexes().isEmpty()) return;
            doc.createParagraph().setPageBreak(true);
            titre("LISTE DES ANNEXES", 15);
            for (ContractModel.Annexe an : m.annexes()) {
                titre(an.numero() + " — " + an.objet(), 12);
                an.elements().forEach(e -> para("• " + e, 11, false));
            }
        }

        private void titre(String texte, int taille) {
            run(doc.createParagraph(), texte, taille, true).setColor(couleurTitre);
        }

        private void para(String texte, int taille, boolean gras) {
            run(doc.createParagraph(), texte, taille, gras);
        }

        private void centre(String texte, int taille, boolean gras) {
            XWPFParagraph p = doc.createParagraph();
            p.setAlignment(ParagraphAlignment.CENTER);
            run(p, texte, taille, gras);
        }

        private XWPFRun run(XWPFParagraph p, String texte, int taille, boolean gras) {
            XWPFRun r = p.createRun();
            r.setText(texte == null ? "" : texte);
            r.setFontFamily(police);
            r.setFontSize(taille);
            r.setBold(gras);
            return r;
        }

        private boolean notBlank(String s) { return s != null && !s.isBlank(); }
    }
}
