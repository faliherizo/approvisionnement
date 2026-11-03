package qc.levis.contrat.clm.domain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

/**
 * Moteur de parcours d'une trousse (ST-07/ST-08) : les étapes d'une même phase s'exécutent en parallèle,
 * les phases se succèdent — un parcours séquentiel n'a qu'une étape par phase, un parcours mixte en combine.
 */
public final class Parcours {

    public enum TypeEtape { VALIDATION, APPROBATION, SIGNATURE }

    public enum StatutEtape { EN_ATTENTE, ACTIVE, APPROUVEE, REJETEE, ANNULEE, NON_REQUISE }

    /** UN_REFUS : un refus suffit à rejeter ; QUORUM_INATTEIGNABLE : rejet seulement si le quorum ne peut plus être atteint. */
    public enum PolitiqueRejet { UN_REFUS, QUORUM_INATTEIGNABLE }

    public enum TypeAffecte { UTILISATEUR, GROUPE, ROLE, PARTICIPANT }

    public record Affectation(TypeAffecte type, String valeur) {
        public Affectation {
            if (type == null || valeur == null || valeur.isBlank()) {
                throw new IllegalArgumentException("Affectation incomplète.");
            }
            valeur = valeur.trim();
        }
    }

    /** {@code conditions} : règle d'affaires (toutes vraies) qui rend l'étape requise ; vide = toujours requise. */
    public record DefinitionEtape(String cle, String libelle, int phase, TypeEtape type, List<Affectation> affectations,
                                  int quorum, PolitiqueRejet politique, Integer delaiJours, List<Modele.Condition> conditions) {
        public DefinitionEtape {
            affectations = affectations == null ? List.of() : List.copyOf(affectations);
            politique = politique == null ? PolitiqueRejet.UN_REFUS : politique;
            conditions = conditions == null ? List.of() : List.copyOf(conditions);
        }

        public DefinitionEtape(String cle, String libelle, int phase, TypeEtape type, List<Affectation> affectations,
                               int quorum, PolitiqueRejet politique, Integer delaiJours) {
            this(cle, libelle, phase, type, affectations, quorum, politique, delaiJours, List.of());
        }
    }

    public record EtatEtape(Long id, int phase, StatutEtape statut) {
    }

    /** Ce que le moteur demande après une décision : étapes à activer et éventuel statut final. */
    public record Avancement(Set<Long> aActiver, Optional<StatutTrousse> resultat) {
    }

    private Parcours() {
    }

    /** Toutes les incohérences d'un parcours, dans l'ordre des étapes (UI-11). */
    public static List<String> valider(List<DefinitionEtape> etapes) {
        List<String> erreurs = new ArrayList<>();
        if (etapes == null || etapes.isEmpty()) {
            erreurs.add("Le parcours doit contenir au moins une étape.");
            return erreurs;
        }
        Set<String> cles = new HashSet<>();
        TreeSet<Integer> phases = new TreeSet<>();
        for (DefinitionEtape e : etapes) {
            String nom = e.libelle() == null || e.libelle().isBlank() ? e.cle() : e.libelle();
            if (e.cle() == null || e.cle().isBlank() || !cles.add(e.cle())) {
                erreurs.add("Clé d'étape absente ou en doublon : " + e.cle());
            }
            if (e.type() == null) {
                erreurs.add("Type d'étape requis : " + nom);
            }
            if (e.phase() < 1) {
                erreurs.add("Phase invalide pour l'étape " + nom);
            }
            phases.add(e.phase());
            validerAffectations(e, nom, erreurs);
            if (e.delaiJours() != null && e.delaiJours() < 1) {
                erreurs.add("Échéance invalide pour l'étape " + nom);
            }
            if (e.conditions().stream().anyMatch(c -> vide(c.champ()) || vide(c.operateur()))) {
                erreurs.add("Règle incomplète pour l'étape " + nom);
            }
        }
        // Chaque étape ajoute sa phase : l'ensemble n'est jamais vide ici.
        if (phases.first() != 1 || phases.last() != phases.size()) {
            erreurs.add("Les phases doivent être consécutives à partir de 1.");
        }
        return erreurs;
    }

    private static boolean vide(String s) {
        return s == null || s.isBlank();
    }

    private static void validerAffectations(DefinitionEtape e, String nom, List<String> erreurs) {
        if (e.affectations().isEmpty()) {
            erreurs.add("Étape sans responsable : " + nom);
            return;
        }
        if (e.quorum() < 1) {
            erreurs.add("Quorum incohérent pour l'étape " + nom);
            return;
        }
        boolean nominatif = e.affectations().stream()
                .allMatch(a -> a.type() == TypeAffecte.UTILISATEUR || a.type() == TypeAffecte.PARTICIPANT);
        if (nominatif && e.quorum() > e.affectations().size()) {
            erreurs.add("Quorum incohérent pour l'étape " + nom);
        }
        if (e.type() == TypeEtape.SIGNATURE && e.affectations().stream().anyMatch(a -> a.type() != TypeAffecte.PARTICIPANT)) {
            erreurs.add("Une étape de signature n'affecte que des participants : " + nom);
        }
    }

    /** Statut d'une étape active après ses décisions. {@code affectes} : nombre de décideurs possibles. */
    public static StatutEtape statutApresDecisions(int quorum, PolitiqueRejet politique, int affectes, int approbations, int refus) {
        if (approbations >= quorum) {
            return StatutEtape.APPROUVEE;
        }
        if (refus > 0 && politique == PolitiqueRejet.UN_REFUS) {
            return StatutEtape.REJETEE;
        }
        if (affectes - refus < quorum) {
            return StatutEtape.REJETEE;
        }
        return StatutEtape.ACTIVE;
    }

    /** Fait progresser le parcours : rejet global, fin, ou activation de la prochaine phase incomplète. */
    public static Avancement avancer(List<EtatEtape> etapes) {
        List<EtatEtape> vivantes = etapes.stream()
                .filter(e -> e.statut() != StatutEtape.ANNULEE && e.statut() != StatutEtape.NON_REQUISE).toList();
        if (vivantes.stream().anyMatch(e -> e.statut() == StatutEtape.REJETEE)) {
            return new Avancement(Set.of(), Optional.of(StatutTrousse.REJETEE));
        }
        Optional<Integer> phase = vivantes.stream()
                .filter(e -> e.statut() != StatutEtape.APPROUVEE)
                .map(EtatEtape::phase).min(Integer::compareTo);
        if (phase.isEmpty()) {
            return new Avancement(Set.of(), Optional.of(StatutTrousse.TERMINEE));
        }
        Set<Long> aActiver = new LinkedHashSet<>();
        for (EtatEtape e : vivantes) {
            if (e.phase() == phase.get() && e.statut() == StatutEtape.EN_ATTENTE) {
                aActiver.add(e.id());
            }
        }
        return new Avancement(aActiver, Optional.empty());
    }
}
