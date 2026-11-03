package qc.levis.contrat.clm.domain;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Périmètre et séparation des responsabilités (ST-02) : qui voit une trousse, qui la modifie, qui décide une étape.
 * Refus par défaut ; l'administrateur voit et administre tout mais reste soumis à la séparation des tâches.
 */
public final class Habilitations {

    public record Acteur(String id, Set<String> roles, Set<String> groupes) {
        public Acteur {
            roles = roles == null ? Set.of() : Set.copyOf(roles);
            groupes = groupes == null ? Set.of() : Set.copyOf(groupes);
        }

        public boolean estAdmin() {
            return roles.contains("ADMIN") || roles.contains("SUPERADMIN");
        }

        /** Auditeur / conformité : consulte tout le tenant, n'intervient sur rien. */
        public boolean estAuditeur() {
            return roles.contains("AUDITEUR");
        }
    }

    private Habilitations() {
    }

    public static boolean correspond(Parcours.Affectation a, Acteur acteur, Map<Long, String> courrielsParticipants) {
        return switch (a.type()) {
            case UTILISATEUR -> a.valeur().equalsIgnoreCase(acteur.id());
            case GROUPE -> acteur.groupes().contains(a.valeur());
            case ROLE -> acteur.roles().contains(a.valeur());
            case PARTICIPANT -> {
                String courriel = courrielsParticipants.get(parseId(a.valeur()));
                yield courriel != null && courriel.equalsIgnoreCase(acteur.id());
            }
        };
    }

    public static boolean peutModifier(Trousse t, Acteur acteur) {
        return acteur.estAdmin() || t.getProprietaire().equalsIgnoreCase(acteur.id());
    }

    /** Visible au propriétaire, à son unité administrative, à tout intervenant désigné et à l'administration. */
    public static boolean peutVoir(Trousse t, Acteur acteur, Collection<Parcours.Affectation> affectations,
                                   Map<Long, String> courrielsParticipants) {
        if (peutModifier(t, acteur)) {
            return true;
        }
        if (t.getUnite() != null && acteur.groupes().contains(t.getUnite())) {
            return true;
        }
        if (courrielsParticipants.values().stream().anyMatch(c -> c.equalsIgnoreCase(acteur.id()))) {
            return true;
        }
        return affectations.stream().anyMatch(a -> correspond(a, acteur, courrielsParticipants));
    }

    /**
     * Consultation : ce que {@link #peutVoir} ouvre, plus tout le tenant pour l'auditeur. La consultation n'ouvre aucune
     * intervention : commenter ou analyser reste réservé à qui {@link #peutVoir} la trousse.
     */
    public static boolean peutConsulter(Trousse t, Acteur acteur, Collection<Parcours.Affectation> affectations,
                                        Map<Long, String> courrielsParticipants) {
        return acteur.estAuditeur() || peutVoir(t, acteur, affectations, courrielsParticipants);
    }

    /** Qui initie une trousse ne valide ni n'approuve son propre dossier ; il peut toutefois la signer. */
    public static void exigerSeparation(Trousse t, Parcours.TypeEtape type, Acteur acteur) {
        if (type != Parcours.TypeEtape.SIGNATURE && t.getProprietaire().equalsIgnoreCase(acteur.id())) {
            throw new IllegalStateException("Séparation des responsabilités : l'initiateur ne peut pas décider cette étape.");
        }
    }

    public static Long parseId(String valeur) {
        try {
            return Long.valueOf(valeur.trim());
        } catch (NumberFormatException e) {
            return -1L;
        }
    }
}
