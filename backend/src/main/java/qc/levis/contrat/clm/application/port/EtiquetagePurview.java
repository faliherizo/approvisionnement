package qc.levis.contrat.clm.application.port;

import java.util.List;
import qc.levis.contrat.clm.domain.EtiquettePurview;

/**
 * Microsoft Purview pour le module (Annexe 10, SEC-27 ; ADR-036) : lecture des étiquettes de sensibilité (Microsoft
 * Information Protection) et de rétention publiées dans le locataire Microsoft 365 de l'organisation, et apposition sur un
 * fichier déposé dans SharePoint ou OneDrive. Adaptateur simulé par défaut ; Microsoft Graph sous le profil
 * {@code purview}, avec la connexion Microsoft 365 de l'organisation (ADR-002, ADR-029). Un échec se lit dans le résultat,
 * jamais par exception : le dépôt reste fait, l'étiquette se réappose à la relance.
 */
public interface EtiquetagePurview {

    /** Étiquettes publiées dans le locataire de l'organisation ; liste vide si la connexion n'est pas disponible. */
    List<EtiquettePurview> catalogue(Long tenantId);

    /** Appose les étiquettes au fichier déposé à cette adresse ; une étiquette nulle n'est pas apposée. */
    Resultat apposer(Long tenantId, String urlDepot, EtiquettePurview sensibilite, EtiquettePurview retention);

    record Resultat(boolean succes, String message) {
    }
}
