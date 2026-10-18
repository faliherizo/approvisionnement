package qc.levis.contrat.integration.application.port;

import qc.levis.contrat.commun.domain.Enums;

/**
 * Port de publication d'un document vers une cible externe (SharePoint, OneDrive, Outlook,
 * Teams, SAP — art. 9). Le domaine/l'application ignorent le protocole (MS Graph, SAP…) :
 * chaque cible est desservie par un adaptateur d'infrastructure (ADR-002, DIP/OCP). Ajouter
 * une cible = ajouter un adaptateur, sans modifier le code appelant.
 */
public interface PublicationCiblePort {

    /** Indique si cet adaptateur sait publier vers la cible demandée. */
    boolean supporte(Enums.CiblePublication cible);

    /** Dépose le contenu rendu vers la cible et renvoie le résultat (URL/état). */
    ResultatPublication deposer(Enums.CiblePublication cible, ContenuPublie contenu);

    /**
     * Contenu rendu prêt à être publié (octets + métadonnées).
     * {@code destination} = répertoire cible (chemin relatif normalisé, ex. « Contrats/2027 »).
     */
    record ContenuPublie(Long documentId, String titre, String nomFichier, String mediaType,
                         byte[] octets, String destination) {}

    /** Résultat d'une publication : succès, URL de dépôt et message. */
    record ResultatPublication(boolean succes, String url, String message) {}
}
