package qc.levis.contrat.saisie.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Places de chaque choix d'un formulaire, comptées sur les fiches qui en occupent une (CHU 3.06 et 4.08). Un choix complet
 * n'est plus proposé et une fiche qui le retient est refusée à la soumission.
 */
public final class PlanDesPlaces {

    private final List<PlaceChoix> places;

    private PlanDesPlaces(List<PlaceChoix> places) {
        this.places = List.copyOf(places);
    }

    /** {@code fiches} : les réponses des fiches qui occupent une place (voir {@link StatutSaisie#occupeUnePlace()}). */
    public static PlanDesPlaces compter(List<ChampAChoix> champs, Collection<Map<String, Object>> fiches) {
        List<PlaceChoix> places = new ArrayList<>();
        for (ChampAChoix champ : champs) {
            Map<String, Integer> inscrits = new HashMap<>();
            for (Map<String, Object> f : fiches) for (String c : retenus(f.get(champ.cle()))) inscrits.merge(c, 1, Integer::sum);
            for (String c : champ.choix()) {
                places.add(new PlaceChoix(champ.cle(), champ.libelle(), c, champ.capacites().get(c), inscrits.getOrDefault(c, 0)));
            }
        }
        return new PlanDesPlaces(places);
    }

    public List<PlaceChoix> places() {
        return places;
    }

    /** Refuse une fiche qui retient un choix complet (le plan doit avoir été compté sans cette fiche). */
    public void exigerDesPlaces(Map<String, Object> reponses) {
        for (PlaceChoix p : places) {
            if (p.complet() && retenus(reponses.get(p.champ())).contains(p.choix())) {
                throw new ChoixComplet("Le choix « " + p.choix() + " » (" + p.libelleChamp() + ") est complet : "
                        + p.capacite() + " place(s), toutes prises.");
            }
        }
    }

    /** Une réponse est un choix unique (texte) ou plusieurs choix (cases à cocher). */
    public static List<String> retenus(Object valeur) {
        if (valeur instanceof Collection<?> plusieurs) return plusieurs.stream().map(String::valueOf).toList();
        return valeur == null || String.valueOf(valeur).isBlank() ? List.of() : List.of(String.valueOf(valeur));
    }

    /** Plus aucune place pour ce choix. */
    public static class ChoixComplet extends IllegalStateException {
        public ChoixComplet(String message) {
            super(message);
        }
    }
}
