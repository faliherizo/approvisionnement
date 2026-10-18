package qc.levis.contrat.commun.application;

import java.util.ArrayList;
import java.util.List;

/** Comparaison texte à texte (redlining) par plus longue sous-séquence commune (LCS). */
public final class DiffUtil {
    private DiffUtil() {}

    public enum Type { EGAL, AJOUT, SUPPRESSION }

    public record Ligne(Type type, String texte) {}

    /** Diff ligne à ligne entre deux textes. */
    public static List<Ligne> diff(String ancien, String nouveau) {
        String[] a = decouper(ancien);
        String[] b = decouper(nouveau);
        int n = a.length, m = b.length;

        // Table LCS
        int[][] lcs = new int[n + 1][m + 1];
        for (int i = n - 1; i >= 0; i--) {
            for (int j = m - 1; j >= 0; j--) {
                lcs[i][j] = a[i].equals(b[j])
                        ? lcs[i + 1][j + 1] + 1
                        : Math.max(lcs[i + 1][j], lcs[i][j + 1]);
            }
        }

        List<Ligne> res = new ArrayList<>();
        int i = 0, j = 0;
        while (i < n && j < m) {
            if (a[i].equals(b[j])) {
                res.add(new Ligne(Type.EGAL, a[i]));
                i++; j++;
            } else if (lcs[i + 1][j] >= lcs[i][j + 1]) {
                res.add(new Ligne(Type.SUPPRESSION, a[i]));
                i++;
            } else {
                res.add(new Ligne(Type.AJOUT, b[j]));
                j++;
            }
        }
        while (i < n) res.add(new Ligne(Type.SUPPRESSION, a[i++]));
        while (j < m) res.add(new Ligne(Type.AJOUT, b[j++]));
        return res;
    }

    private static String[] decouper(String s) {
        return (s == null ? "" : s).split("\n", -1);
    }
}
