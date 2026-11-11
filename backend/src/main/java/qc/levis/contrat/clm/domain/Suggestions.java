package qc.levis.contrat.clm.domain;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Saisie assistée (F-04) : valeurs déjà retenues pour un même champ, de la plus fréquente à la moins fréquente ; à
 * fréquence égale, la plus récente d'abord. Les jeux de valeurs sont fournis du plus récent au plus ancien.
 */
public final class Suggestions {

    public static final int MAX = 8;

    private Suggestions() {
    }

    public static List<String> pour(String cle, List<Map<String, Preremplissage.Valeur>> jeux, String valeurActuelle) {
        Map<String, Integer> frequences = new LinkedHashMap<>();
        for (Map<String, Preremplissage.Valeur> jeu : jeux) {
            Preremplissage.Valeur v = jeu.get(cle);
            String texte = v == null || v.valeur() == null ? "" : String.valueOf(v.valeur()).strip();
            if (!texte.isEmpty() && !Objects.equals(texte, valeurActuelle == null ? null : valeurActuelle.strip())) {
                frequences.merge(texte, 1, Integer::sum);
            }
        }
        List<String> ordreRecence = List.copyOf(frequences.keySet());
        return frequences.keySet().stream()
                .sorted(Comparator.comparing((String s) -> -frequences.get(s)).thenComparing(ordreRecence::indexOf))
                .limit(MAX).toList();
    }
}
