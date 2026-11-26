package qc.levis.contrat.clm.domain;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Repli déterministe de la négociation assistée (RST-55) : la proposition est mesurée contre la clause normative du modèle
 * de l'organisation (texte de la version 1). Elle retire un élément attendu (liste « Clause attendue ») : refuser ; elle
 * reste proche du texte normatif : accepter ; sinon : contre-proposer. Le résultat reste une suggestion à décider.
 */
public final class RecommandationNegociation {

    /** Au-delà de cette part de mots communs, l'écart est jugé mineur. */
    public static final double SIMILARITE_MINEURE = 0.85;

    public record Recommandation(PositionNegociation position, String resume, double similarite, List<String> elementsRetires) {
    }

    private RecommandationNegociation() {
    }

    /** @param attendus élément attendu → mots-clés qui le signalent (mots-clés déjà normalisés, sans casse ni accents) */
    public static Recommandation recommander(ClauseNegociee normative, String propose, Map<String, List<String>> attendus) {
        double similarite = Math.round(DiffTexte.similarite(normative.texte(), propose) * 100) / 100.0;
        List<String> retires = retires(normaliser(normative.texte()), normaliser(propose), attendus);
        PositionNegociation position = !retires.isEmpty() ? PositionNegociation.REFUSER
                : similarite >= SIMILARITE_MINEURE ? PositionNegociation.ACCEPTER : PositionNegociation.CONTRE_PROPOSER;
        return new Recommandation(position, resume(normative, propose, similarite, retires), similarite, retires);
    }

    private static List<String> retires(String normatif, String propose, Map<String, List<String>> attendus) {
        return attendus.entrySet().stream()
                .filter(e -> e.getValue().stream().anyMatch(normatif::contains) && e.getValue().stream().noneMatch(propose::contains))
                .map(Map.Entry::getKey).sorted().toList();
    }

    private static String resume(ClauseNegociee normative, String propose, double similarite, List<String> retires) {
        int[] n = DiffTexte.compte(DiffTexte.comparer(normative.texte(), propose));
        String texte = "Écart sur « " + normative.titre() + " » : " + n[1] + " mot(s) ajouté(s), " + n[0] + " retiré(s) ; "
                + Math.round(similarite * 100) + " % de similarité avec la clause normative.";
        return retires.isEmpty() ? texte : texte + " Retire un élément attendu : " + String.join(", ", retires) + ".";
    }

    static String normaliser(String s) {
        String t = s == null ? "" : s;
        return Normalizer.normalize(t, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }
}
