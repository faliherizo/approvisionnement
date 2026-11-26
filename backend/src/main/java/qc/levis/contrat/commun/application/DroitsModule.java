package qc.levis.contrat.commun.application;

import java.util.List;

/**
 * Port de lecture des droits qu'un module accorde à un compte (TI-21, RST-52) : la découverte des droits par l'annuaire
 * (SCIM) les expose sans que le module d'authentification dépende d'aucun module. Chaque module qui gère des groupes en
 * fournit un adaptateur ; aucun n'en écrit par ce port.
 */
public interface DroitsModule {

    /** Droits accordés par le module au compte de ce courriel, dans l'organisation ; liste vide s'il n'en a aucun. */
    List<DroitModule> droits(Long tenantId, String courriel);
}
