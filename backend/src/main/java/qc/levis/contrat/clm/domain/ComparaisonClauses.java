package qc.levis.contrat.clm.domain;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Écarts entre deux versions négociées (RST-55), clause par clause : celles de la version de départ dans leur ordre,
 * puis celles qui n'existent que dans la version d'arrivée. Chaque écart porte le détail mot à mot.
 */
public final class ComparaisonClauses {

    public enum Changement { INCHANGEE, MODIFIEE, AJOUTEE, RETIREE }

    public record Ecart(String cle, String titre, Changement changement, List<DiffTexte.Segment> segments) {
    }

    private ComparaisonClauses() {
    }

    public static List<Ecart> comparer(List<ClauseNegociee> avant, List<ClauseNegociee> apres) {
        Map<String, ClauseNegociee> restantes = new LinkedHashMap<>();
        apres.forEach(c -> restantes.put(c.cle(), c));
        List<Ecart> ecarts = new ArrayList<>();
        for (ClauseNegociee a : avant) {
            ClauseNegociee b = restantes.remove(a.cle());
            ecarts.add(b == null ? ecart(a, Changement.RETIREE, a.texte(), "") : ecart(a, changement(a, b), a.texte(), b.texte()));
        }
        restantes.values().forEach(b -> ecarts.add(ecart(b, Changement.AJOUTEE, "", b.texte())));
        return ecarts;
    }

    private static Changement changement(ClauseNegociee a, ClauseNegociee b) {
        return a.texte().equals(b.texte()) ? Changement.INCHANGEE : Changement.MODIFIEE;
    }

    private static Ecart ecart(ClauseNegociee c, Changement changement, String avant, String apres) {
        return new Ecart(c.cle(), c.titre(), changement, DiffTexte.comparer(avant, apres));
    }
}
