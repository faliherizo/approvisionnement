package qc.levis.contrat.clm.infrastructure;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.poi.xwpf.usermodel.IBody;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFHeaderFooter;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.apache.xmlbeans.XmlCursor;
import qc.levis.contrat.clm.application.port.RenduDocuments;
import qc.levis.contrat.clm.domain.Modele;

/**
 * Fusion des variables dans le gabarit Word d'origine (addenda 4, Q22). Word découpe souvent un texte en plusieurs
 * « runs » (correcteur, révisions, mise en forme) : une variable {@code {{cle}}} peut donc s'étaler sur plusieurs runs.
 * Le remplacement se fait sur le texte reconstitué du paragraphe, puis s'écrit dans le premier run concerné, qui en
 * garde la mise en forme ; les autres runs ne perdent que la partie de variable qu'ils portaient.
 */
final class FusionDocx {

    private static final Pattern VARIABLE = Pattern.compile("\\{\\{\\s*([\\w.-]+)\\s*}}");
    private static final Pattern MARQUEUR_SEUL = Pattern.compile("^\\s*\\{\\{\\s*" + Modele.MARQUEUR_CLAUSES + "\\s*}}\\s*$");

    private FusionDocx() {
    }

    static byte[] fusionner(byte[] gabarit, Map<String, Object> valeurs, List<RenduDocuments.ClauseFusionnee> clauses) {
        try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(gabarit)); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            boolean marqueur = false;
            for (XWPFParagraph p : corps(doc)) {
                if (MARQUEUR_SEUL.matcher(p.getText()).matches()) {
                    inserer(p, clauses);
                    marqueur = true;
                } else {
                    remplacer(p, valeurs);
                }
            }
            for (XWPFParagraph p : entetesEtPiedsDePage(doc)) {
                if (MARQUEUR_SEUL.matcher(p.getText()).matches()) {
                    p.getRuns().forEach(r -> ecrire(r, ""));
                } else {
                    remplacer(p, valeurs);
                }
            }
            if (!marqueur) {
                clauses.forEach(c -> ajouter(doc, c));
            }
            doc.write(out);
            return out.toByteArray();
        } catch (IOException | RuntimeException e) {
            throw new IllegalStateException("Gabarit Word illisible : " + e.getMessage(), e);
        }
    }

    /** Variables du gabarit, telles que Word les affiche (runs réunis). */
    static List<String> variables(byte[] gabarit) {
        try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(gabarit))) {
            List<String> vues = new ArrayList<>();
            for (XWPFParagraph p : paragraphes(doc)) {
                Matcher m = VARIABLE.matcher(p.getText());
                while (m.find()) {
                    vues.add(m.group(1));
                }
            }
            return vues;
        } catch (IOException | RuntimeException e) {
            throw new IllegalArgumentException("Gabarit Word illisible : " + e.getMessage(), e);
        }
    }

    /** Tous les paragraphes : corps, tableaux, en-têtes et pieds de page. */
    private static List<XWPFParagraph> paragraphes(XWPFDocument doc) {
        List<XWPFParagraph> tous = corps(doc);
        tous.addAll(entetesEtPiedsDePage(doc));
        return tous;
    }

    /** Paragraphes du corps et de ses tableaux : seuls points d'insertion des clauses. */
    private static List<XWPFParagraph> corps(XWPFDocument doc) {
        List<XWPFParagraph> tous = new ArrayList<>(doc.getParagraphs());
        doc.getTables().forEach(t -> deTableau(t, tous));
        return tous;
    }

    /** Un marqueur de clauses y est vidé : un en-tête ou un pied de page ne reçoit pas le texte des clauses. */
    private static List<XWPFParagraph> entetesEtPiedsDePage(XWPFDocument doc) {
        List<XWPFParagraph> tous = new ArrayList<>();
        List<XWPFHeaderFooter> hf = new ArrayList<>(doc.getHeaderList());
        hf.addAll(doc.getFooterList());
        for (XWPFHeaderFooter h : hf) {
            tous.addAll(h.getParagraphs());
            h.getTables().forEach(t -> deTableau(t, tous));
        }
        return tous;
    }

    private static void deTableau(XWPFTable t, List<XWPFParagraph> tous) {
        for (XWPFTableRow r : t.getRows()) {
            for (XWPFTableCell c : r.getTableCells()) {
                tous.addAll(c.getParagraphs());
                c.getTables().forEach(imbrique -> deTableau(imbrique, tous));
            }
        }
    }

    /** Remplacements de la fin vers le début : les positions des variables précédentes restent valides. */
    static void remplacer(XWPFParagraph p, Map<String, Object> valeurs) {
        List<XWPFRun> runs = p.getRuns();
        String texte = texte(runs);
        Matcher m = VARIABLE.matcher(texte);
        List<int[]> positions = new ArrayList<>();
        List<String> remplacements = new ArrayList<>();
        while (m.find()) {
            positions.add(new int[]{m.start(), m.end()});
            remplacements.add(Modele.fusionner(m.group(), valeurs));
        }
        for (int i = positions.size() - 1; i >= 0; i--) {
            remplacerPlage(runs, positions.get(i), remplacements.get(i));
        }
    }

    private static void remplacerPlage(List<XWPFRun> runs, int[] plage, String valeur) {
        int debut = 0;
        boolean ecrit = false;
        for (XWPFRun r : runs) {
            String t = texte(r);
            int fin = debut + t.length();
            if (fin > plage[0] && debut < plage[1]) {
                String avant = t.substring(0, Math.max(0, plage[0] - debut));
                String apres = t.substring(Math.min(t.length(), plage[1] - debut));
                ecrire(r, ecrit ? avant + apres : avant + valeur + apres);
                ecrit = true;
            }
            debut = fin;
        }
    }

    private static String texte(List<XWPFRun> runs) {
        StringBuilder sb = new StringBuilder();
        runs.forEach(r -> sb.append(texte(r)));
        return sb.toString();
    }

    private static String texte(XWPFRun r) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < r.getCTR().sizeOfTArray(); i++) {
            sb.append(r.getCTR().getTArray(i).getStringValue());
        }
        return sb.toString();
    }

    private static void ecrire(XWPFRun r, String texte) {
        for (int i = r.getCTR().sizeOfTArray() - 1; i > 0; i--) {
            r.getCTR().removeT(i);
        }
        r.setText(texte, 0);
    }

    /** Clauses à la place du paragraphe marqueur, dans son style ; titre en gras, un paragraphe par ligne. */
    private static void inserer(XWPFParagraph marqueur, List<RenduDocuments.ClauseFusionnee> clauses) {
        IBody corps = marqueur.getBody();
        for (RenduDocuments.ClauseFusionnee c : clauses) {
            ecrireClause(c, () -> {
                XmlCursor curseur = marqueur.getCTP().newCursor();
                XWPFParagraph p = corps.insertNewParagraph(curseur);
                curseur.dispose();
                p.setStyle(marqueur.getStyle());
                return p;
            });
        }
        retirer(marqueur);
    }

    private static void ajouter(XWPFDocument doc, RenduDocuments.ClauseFusionnee c) {
        ecrireClause(c, doc::createParagraph);
    }

    private static void ecrireClause(RenduDocuments.ClauseFusionnee c, java.util.function.Supplier<XWPFParagraph> nouveau) {
        if (c.titre() != null && !c.titre().isBlank()) {
            XWPFRun titre = nouveau.get().createRun();
            titre.setBold(true);
            titre.setText(c.titre());
        }
        for (String ligne : (c.texte() == null ? "" : c.texte()).split("\n", -1)) {
            nouveau.get().createRun().setText(ligne);
        }
    }

    private static void retirer(XWPFParagraph p) {
        if (p.getBody() instanceof XWPFDocument d) {
            d.removeBodyElement(d.getPosOfParagraph(p));
        } else {
            XWPFTableCell c = (XWPFTableCell) p.getBody();
            c.removeParagraph(c.getParagraphs().indexOf(p));
        }
    }
}
