package qc.levis.contrat.auth.domain;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

/**
 * Application des correspondances d'annuaire aux faits d'un compte (addenda 3, Q43-Q44). Moindre privilège : un compte
 * ne reçoit que les rôles de ses correspondances ; à défaut d'aucune, le rôle par défaut de l'organisation. Les rôles ne
 * sont pilotés par l'annuaire que si l'organisation a défini au moins une correspondance de rôle.
 */
public final class ReglesAnnuaire {

    /**
     * @param roles         rôles à appliquer ; vide si {@code rolesGeres} est faux (les rôles en place sont conservés)
     * @param groupes       domaines d'affaires du compte
     * @param groupesGeres  domaines d'affaires pilotés par l'annuaire : les seuls que la synchronisation ajoute ou retire
     */
    public record Resultat(Set<String> roles, boolean rolesGeres, Set<String> groupes, Set<String> groupesGeres) {
    }

    private ReglesAnnuaire() {
    }

    public static Resultat appliquer(Collection<CorrespondanceAnnuaire> regles, Collection<FaitAnnuaire> faits, String roleParDefaut) {
        Set<String> roles = new TreeSet<>();
        Set<String> groupes = new TreeSet<>();
        Set<String> groupesGeres = new TreeSet<>();
        boolean rolesGeres = false;
        for (CorrespondanceAnnuaire r : regles) {
            rolesGeres |= r.getRole() != null;
            if (r.getGroupe() != null) {
                groupesGeres.add(r.getGroupe());
            }
            if (faits.stream().anyMatch(r::satisfaitePar)) {
                if (r.getRole() != null) {
                    roles.add(r.getRole());
                }
                if (r.getGroupe() != null) {
                    groupes.add(r.getGroupe());
                }
            }
        }
        if (rolesGeres && roles.isEmpty() && roleParDefaut != null) {
            roles.add(roleParDefaut);
        }
        return new Resultat(rolesGeres ? roles : Set.of(), rolesGeres, groupes, groupesGeres);
    }
}
