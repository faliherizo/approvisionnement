package qc.levis.contrat.clm.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Comparaison de deux textes mot à mot (RST-55) : plus longue sous-suite commune, puis segments égaux, ajoutés ou
 * retirés, dans l'ordre de lecture. Au-delà de {@link #MAX_MOTS} mots, le coût quadratique n'est plus raisonnable : le
 * texte est alors comparé d'un bloc (tout retiré, tout ajouté).
 */
public final class DiffTexte {

    public static final int MAX_MOTS = 2_000;

    public enum Nature { EGAL, AJOUT, RETRAIT }

    public record Segment(Nature nature, String texte) {
    }

    private DiffTexte() {
    }

    public static List<Segment> comparer(String avant, String apres) {
        String[] a = mots(avant);
        String[] b = mots(apres);
        if (a.length > MAX_MOTS || b.length > MAX_MOTS) {
            return enBloc(a, b);
        }
        return fusionner(parcourir(a, b, longueurs(a, b)));
    }

    /** Part des mots communs (coefficient de Dice) : 1 pour deux textes identiques, 0 sans aucun mot commun. */
    public static double similarite(String avant, String apres) {
        String[] a = mots(avant);
        String[] b = mots(apres);
        if (a.length + b.length == 0) {
            return 1.0;
        }
        long communs = comparer(avant, apres).stream().filter(s -> s.nature() == Nature.EGAL)
                .mapToLong(s -> mots(s.texte()).length).sum();
        return 2.0 * communs / (a.length + b.length);
    }

    /** Mots retirés et ajoutés, dans cet ordre. */
    public static int[] compte(List<Segment> segments) {
        int[] n = new int[2];
        for (Segment s : segments) {
            if (s.nature() != Nature.EGAL) {
                n[s.nature() == Nature.RETRAIT ? 0 : 1] += mots(s.texte()).length;
            }
        }
        return n;
    }

    static String[] mots(String texte) {
        String t = texte == null ? "" : texte.strip();
        return t.isEmpty() ? new String[0] : t.split("\\s+");
    }

    private static List<Segment> enBloc(String[] a, String[] b) {
        if (Arrays.equals(a, b)) {
            return List.of(new Segment(Nature.EGAL, String.join(" ", a)));
        }
        return List.of(new Segment(Nature.RETRAIT, String.join(" ", a)), new Segment(Nature.AJOUT, String.join(" ", b)));
    }

    private static int[][] longueurs(String[] a, String[] b) {
        int[][] l = new int[a.length + 1][b.length + 1];
        for (int i = a.length - 1; i >= 0; i--) {
            for (int j = b.length - 1; j >= 0; j--) {
                l[i][j] = a[i].equals(b[j]) ? l[i + 1][j + 1] + 1 : Math.max(l[i + 1][j], l[i][j + 1]);
            }
        }
        return l;
    }

    private static List<Segment> parcourir(String[] a, String[] b, int[][] l) {
        List<Segment> brut = new ArrayList<>();
        int i = 0;
        int j = 0;
        while (i < a.length || j < b.length) {
            if (i < a.length && j < b.length && a[i].equals(b[j])) {
                brut.add(new Segment(Nature.EGAL, a[i++]));
                j++;
            } else if (j < b.length && (i == a.length || l[i][j + 1] >= l[i + 1][j])) {
                brut.add(new Segment(Nature.AJOUT, b[j++]));
            } else {
                brut.add(new Segment(Nature.RETRAIT, a[i++]));
            }
        }
        return brut;
    }

    private static List<Segment> fusionner(List<Segment> brut) {
        List<Segment> res = new ArrayList<>();
        for (Segment s : brut) {
            Segment dernier = res.isEmpty() ? null : res.get(res.size() - 1);
            if (dernier != null && dernier.nature() == s.nature()) {
                res.set(res.size() - 1, new Segment(s.nature(), dernier.texte() + " " + s.texte()));
            } else {
                res.add(s);
            }
        }
        return res;
    }
}
