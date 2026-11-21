package qc.levis.contrat.commun.infrastructure.pdf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;

/**
 * Document PDF/A-2b composé de blocs (titre, paragraphe, libellé et valeur, tableau), avec retour à la ligne et sauts de page.
 * Sert aux documents produits depuis les données (fiche, rapport) ; la conformité vient de {@link ConformitePdfA}.
 */
public final class DocumentPdfA {

    /** Élément de contenu, dans l'ordre de lecture. */
    public sealed interface Bloc permits Titre, Paragraphe, Paire, Tableau { }
    public record Titre(String texte) implements Bloc { }
    public record Paragraphe(String texte) implements Bloc { }
    public record Paire(String libelle, String valeur) implements Bloc { }
    public record Tableau(List<String> colonnes, List<List<String>> lignes) implements Bloc {
        public Tableau {
            colonnes = List.copyOf(colonnes);
            lignes = lignes.stream().map(l -> l.stream().map(v -> v == null ? "" : v).toList()).toList();
        }
    }

    static final float MARGE = 50;
    static final float TAILLE_TITRE = 15;
    static final float TAILLE = 10;
    static final float INTERLIGNE = 1.35f;

    private DocumentPdfA() { }

    public static byte[] produire(String titre, List<Bloc> blocs, Calendar le) {
        try (PDDocument doc = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Mise mise = new Mise(doc, ConformitePdfA.police(doc));
            mise.titre(titre);
            for (Bloc b : blocs) mise.bloc(b);
            mise.fermer();
            ConformitePdfA.declarer(doc, titre, le);
            doc.save(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Production du document PDF impossible : " + e.getMessage(), e);
        }
    }

    /** Écrit les blocs page après page ; la position verticale descend, une nouvelle page s'ouvre au bas de la page. */
    private static final class Mise {
        private final PDDocument doc;
        private final PDFont police;
        private final float largeur = PDRectangle.LETTER.getWidth() - 2 * MARGE;
        private PDPageContentStream cs;
        private float y;

        Mise(PDDocument doc, PDFont police) throws IOException {
            this.doc = doc;
            this.police = police;
            nouvellePage();
        }

        void bloc(Bloc b) throws IOException {
            if (b instanceof Titre t) titre(t.texte());
            else if (b instanceof Paragraphe p) lignes(p.texte(), MARGE, largeur, TAILLE);
            else if (b instanceof Paire p) paire(p);
            else tableau((Tableau) b);
        }

        void titre(String texte) throws IOException {
            y -= 6;
            lignes(texte, MARGE, largeur, TAILLE_TITRE);
            y -= 4;
        }

        private void paire(Paire p) throws IOException {
            float colonne = largeur * 0.35f;
            List<String> gauche = couper(p.libelle(), colonne - 8, TAILLE);
            List<String> droite = couper(p.valeur(), largeur - colonne, TAILLE);
            rangee(List.of(gauche, droite), List.of(MARGE, MARGE + colonne));
        }

        private void tableau(Tableau t) throws IOException {
            float col = largeur / Math.max(1, t.colonnes().size());
            List<Float> xs = new ArrayList<>();
            for (int i = 0; i < t.colonnes().size(); i++) xs.add(MARGE + i * col);
            rangee(cellules(t.colonnes(), col - 6), xs);
            for (List<String> l : t.lignes()) rangee(cellules(l, col - 6), xs);
            y -= 4;
        }

        private List<List<String>> cellules(List<String> valeurs, float largeurCellule) throws IOException {
            List<List<String>> c = new ArrayList<>();
            for (String v : valeurs) c.add(couper(v, largeurCellule, TAILLE));
            return c;
        }

        /** Cellules côte à côte ; la rangée prend la hauteur de sa cellule la plus longue. */
        private void rangee(List<List<String>> cellules, List<Float> xs) throws IOException {
            int hauteur = cellules.stream().mapToInt(List::size).max().orElse(1);
            if (y - hauteur * TAILLE * INTERLIGNE < MARGE) nouvellePage();
            for (int i = 0; i < hauteur; i++) {
                for (int c = 0; c < cellules.size(); c++) {
                    if (i < cellules.get(c).size()) ecrire(xs.get(c), cellules.get(c).get(i), TAILLE);
                }
                y -= TAILLE * INTERLIGNE;
            }
        }

        private void lignes(String texte, float x, float largeurUtile, float taille) throws IOException {
            for (String l : couper(texte, largeurUtile, taille)) {
                if (y - taille * INTERLIGNE < MARGE) nouvellePage();
                ecrire(x, l, taille);
                y -= taille * INTERLIGNE;
            }
        }

        private void ecrire(float x, String ligne, float taille) throws IOException {
            cs.beginText();
            cs.setFont(police, taille);
            cs.newLineAtOffset(x, y - taille);
            cs.showText(ligne);
            cs.endText();
        }

        /** Coupe au mot ; un mot plus long que la largeur est coupé lui-même. */
        List<String> couper(String texte, float largeurUtile, float taille) throws IOException {
            List<String> resultat = new ArrayList<>();
            for (String para : ConformitePdfA.affichable(police, texte == null ? "" : texte).split("\n", -1)) {
                StringBuilder courante = new StringBuilder();
                for (String mot : para.split(" ")) {
                    String essai = courante.isEmpty() ? mot : courante + " " + mot;
                    if (largeurDe(essai, taille) <= largeurUtile) { courante.setLength(0); courante.append(essai); continue; }
                    if (!courante.isEmpty()) resultat.add(courante.toString());
                    courante.setLength(0);
                    courante.append(trancher(mot, largeurUtile, taille, resultat));
                }
                resultat.add(courante.toString());
            }
            return resultat;
        }

        private String trancher(String mot, float largeurUtile, float taille, List<String> resultat) throws IOException {
            String reste = mot;
            while (reste.length() > 1 && largeurDe(reste, taille) > largeurUtile) {
                int n = reste.length();
                while (n > 1 && largeurDe(reste.substring(0, n), taille) > largeurUtile) n--;
                resultat.add(reste.substring(0, n));
                reste = reste.substring(n);
            }
            return reste;
        }

        private float largeurDe(String s, float taille) throws IOException {
            return police.getStringWidth(s) / 1000 * taille;
        }

        private void nouvellePage() throws IOException {
            if (cs != null) cs.close();
            PDPage page = new PDPage(PDRectangle.LETTER);
            doc.addPage(page);
            cs = new PDPageContentStream(doc, page);
            y = page.getMediaBox().getHeight() - MARGE;
        }

        void fermer() throws IOException {
            cs.close();
        }
    }
}
