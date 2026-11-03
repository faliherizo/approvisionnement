package qc.levis.contrat.commun.module.application;

import java.util.*;
import qc.levis.contrat.commun.module.domain.ModuleCatalogue;

/**
 * Calcul PUR (sans E/S) de la résolution des dépendances entre modules — testable isolément.
 * Un module « requiert » d'autres modules (prérequis) ; activer un module active ses prérequis
 * transitifs, désactiver un module désactive ses dépendants transitifs.
 */
public final class ModuleDependances {

    private ModuleDependances() {}

    /** Découpe la liste de prérequis « a,b,c » en codes (vide si null/blanc). */
    public static List<String> parse(String requiert) {
        if (requiert == null || requiert.isBlank()) return List.of();
        List<String> res = new ArrayList<>();
        for (String c : requiert.split(",")) {
            String t = c.trim();
            if (!t.isEmpty()) res.add(t);
        }
        return res;
    }

    /** Codes à activer avec {@code code} : lui-même + tous ses prérequis transitifs. */
    public static Set<String> requisTransitifs(List<ModuleCatalogue> catalogue, String code) {
        Map<String, List<String>> req = new HashMap<>();
        for (ModuleCatalogue m : catalogue) req.put(m.getCode(), parse(m.getRequiert()));
        Set<String> vus = new LinkedHashSet<>();
        Deque<String> pile = new ArrayDeque<>();
        pile.push(code);
        while (!pile.isEmpty()) {
            String c = pile.pop();
            if (!vus.add(c)) continue;
            for (String r : req.getOrDefault(c, List.of())) pile.push(r);
        }
        return vus;
    }

    /** Codes à désactiver avec {@code code} : lui-même + tous les modules qui en dépendent transitivement. */
    public static Set<String> dependantsTransitifs(List<ModuleCatalogue> catalogue, String code) {
        // graphe inverse : pour chaque module, qui le requiert directement
        Map<String, List<String>> inverse = new HashMap<>();
        for (ModuleCatalogue m : catalogue) {
            for (String r : parse(m.getRequiert())) {
                inverse.computeIfAbsent(r, k -> new ArrayList<>()).add(m.getCode());
            }
        }
        Set<String> vus = new LinkedHashSet<>();
        Deque<String> pile = new ArrayDeque<>();
        pile.push(code);
        while (!pile.isEmpty()) {
            String c = pile.pop();
            if (!vus.add(c)) continue;
            for (String d : inverse.getOrDefault(c, List.of())) pile.push(d);
        }
        return vus;
    }
}
