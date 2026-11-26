package qc.levis.contrat.clm.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Modification contrôlée des champs d'une trousse en cours de traitement (Annexe 10, AF-34 ; ADR-030). Le modèle énumère
 * les champs modifiables une fois le parcours lancé : à quelles étapes, par quel rôle, et avec quel effet sur les
 * approbations déjà obtenues. Refus par défaut : un champ absent de la liste n'est plus modifiable dès le lancement.
 */
public final class ModificationEnCours {

    /** Effet d'une modification sur les décisions déjà recueillies, du plus léger au plus lourd. */
    public enum Effet { AUCUN, REPRISE_ETAPE, NOUVEAU_CYCLE }

    /**
     * Règle d'un champ. {@code etapes} : clés des étapes pendant lesquelles la modification est permise (vide = toutes) ;
     * {@code roleRequis} : rôle exigé de l'auteur (vide = quiconque gère la trousse).
     */
    public record Regle(String champ, List<String> etapes, String roleRequis, Effet effet) {
        public Regle {
            etapes = etapes == null ? List.of() : etapes.stream().filter(e -> e != null && !e.isBlank()).map(String::strip).toList();
            roleRequis = roleRequis == null || roleRequis.isBlank() ? null : roleRequis.strip();
            effet = effet == null ? Effet.NOUVEAU_CYCLE : effet;
        }
    }

    /** Verdict sur un ensemble de champs modifiés : motifs de refus, et l'effet le plus lourd des champs admis. */
    public record Verdict(List<String> refus, Effet effet) {
        public boolean admis() {
            return refus.isEmpty();
        }
    }

    private ModificationEnCours() {
    }

    /** Cohérence avec le modèle : chaque règle vise un champ connu, une seule fois, et des étapes du parcours du modèle. */
    public static List<String> valider(List<Regle> regles, Set<String> champs, Set<String> etapes) {
        List<String> erreurs = new ArrayList<>();
        Set<String> vus = new HashSet<>();
        for (Regle r : regles) {
            if (r.champ() == null || !champs.contains(r.champ())) {
                erreurs.add("Modification en cours : champ inconnu « " + r.champ() + " ».");
            } else if (!vus.add(r.champ())) {
                erreurs.add("Modification en cours : champ « " + r.champ() + " » défini deux fois.");
            }
            r.etapes().stream().filter(e -> !etapes.contains(e))
                    .forEach(e -> erreurs.add("Modification en cours de « " + r.champ() + " » : étape inconnue « " + e + " »."));
        }
        return erreurs;
    }

    /**
     * Décision pour les champs modifiés pendant le traitement. {@code libelles} nomme les champs dans les motifs ;
     * {@code etapesActives} : clés des étapes en cours ; {@code roles} : rôles de l'auteur.
     */
    public static Verdict autoriser(List<Regle> regles, Collection<String> modifies, Map<String, String> libelles,
                                    Set<String> etapesActives, Set<String> roles) {
        List<String> refus = new ArrayList<>();
        Effet effet = Effet.AUCUN;
        for (String champ : modifies) {
            String nom = libelles.getOrDefault(champ, champ);
            Regle r = regles.stream().filter(x -> champ.equals(x.champ())).findFirst().orElse(null);
            if (r == null) {
                refus.add("« " + nom + " » n'est pas modifiable en cours de traitement.");
            } else if (!r.etapes().isEmpty() && r.etapes().stream().noneMatch(etapesActives::contains)) {
                refus.add("« " + nom + " » n'est modifiable qu'aux étapes " + String.join(", ", r.etapes()) + ".");
            } else if (r.roleRequis() != null && !roles.contains(r.roleRequis())) {
                refus.add("« " + nom + " » exige le rôle " + r.roleRequis() + ".");
            } else if (r.effet().compareTo(effet) > 0) {
                effet = r.effet();
            }
        }
        return new Verdict(List.copyOf(refus), effet);
    }
}
