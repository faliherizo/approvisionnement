package qc.levis.contrat.auth.application.dto;

import java.util.List;
import qc.levis.contrat.commun.application.DroitModule;

/**
 * Compte vu par SCIM.
 *
 * @param roles  rôles effectifs de la plateforme (RST-52), en lecture seule
 * @param droits droits accordés par les modules, dont les groupes du module CLM (RST-52), en lecture seule
 */
public record ScimUtilisateurVue(String id, String userName, String nom, boolean actif, List<String> groupes,
                                 List<String> roles, List<DroitModule> droits) {
}
