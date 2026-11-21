package qc.levis.contrat.rapport.domain;

import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Calcul pur d'un indicateur sur des fiches : des points (étiquette, valeur), jamais de renseignement sur une fiche. */
public final class Agregation {

    public record Fiche(String statut, Instant soumiseLe, Map<String, Object> reponses) {}

    public record Point(String etiquette, long valeur) {}

    static final ZoneId FUSEAU = ZoneId.of("America/Toronto");

    private Agregation() { }

    public static List<Point> calculer(Indicateur i, List<Fiche> fiches) {
        List<Fiche> retenues = fiches.stream().filter(f -> i.statuts().isEmpty() || i.statuts().contains(f.statut())).toList();
        if (i.type() == Indicateur.Type.COMPTEUR) return List.of(new Point("Fiches", retenues.size()));
        Map<String, Long> groupes = new TreeMap<>();
        for (Fiche f : retenues) for (String g : groupes(i, f)) groupes.merge(g, 1L, Long::sum);
        return groupes.entrySet().stream().map(e -> new Point(e.getKey(), e.getValue())).toList();
    }

    private static List<String> groupes(Indicateur i, Fiche f) {
        return switch (i.regroupement()) {
            case STATUT -> List.of(f.statut());
            case MOIS -> f.soumiseLe() == null ? List.of() : List.of(YearMonth.from(f.soumiseLe().atZone(FUSEAU)).toString());
            case CHAMP -> valeurs(f.reponses().get(i.champ()));
            case AUCUN -> List.of();
        };
    }

    private static List<String> valeurs(Object v) {
        if (v instanceof Collection<?> plusieurs) return plusieurs.stream().map(String::valueOf).toList();
        return v == null || String.valueOf(v).isBlank() ? List.of("(sans réponse)") : List.of(String.valueOf(v));
    }
}
