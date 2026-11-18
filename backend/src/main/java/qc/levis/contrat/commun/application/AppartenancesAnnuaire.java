package qc.levis.contrat.commun.application;

import java.util.Set;

/**
 * Événement publié quand les domaines d'affaires d'un compte, tirés de l'annuaire, sont recalculés (addenda 3, Q44).
 * Chaque module qui gère des groupes s'y abonne sans dépendre du module d'authentification.
 *
 * @param groupes      domaines d'affaires du compte
 * @param groupesGeres domaines pilotés par l'annuaire : les seuls à ajouter ou retirer ; les autres restent manuels
 */
public record AppartenancesAnnuaire(Long tenantId, String courriel, Set<String> groupes, Set<String> groupesGeres) {
}
