package qc.levis.contrat.integration.application.port;

import qc.levis.contrat.commun.domain.Enums;

/**
 * Client Microsoft Graph pour un tenant donné (dépôt de fichiers vers SharePoint/OneDrive/Teams,
 * envoi Outlook, test de connexion). La configuration est fournie <em>déchiffrée</em> par la couche
 * application ; l'adaptateur d'infrastructure (HTTP/Graph) ignore la base et le chiffrement (DIP).
 */
public interface Microsoft365Client {

    /** Dépose le contenu rendu vers la cible et renvoie le résultat (URL/état). */
    PublicationCiblePort.ResultatPublication deposer(ConfigM365 config, Enums.CiblePublication cible,
                                                     PublicationCiblePort.ContenuPublie contenu);

    /** Vérifie que les identifiants permettent d'obtenir un jeton (test de connexion). */
    PublicationCiblePort.ResultatPublication tester(ConfigM365 config);

    /** Configuration M365 déchiffrée, prête à l'usage (aucun secret persisté ici). */
    record ConfigM365(String msTenantId, String clientId, String clientSecretClair,
                      String sharepointSiteId, String driveId, String teamsTeamId,
                      String teamsChannelId, String outlookExpediteur) {}
}
