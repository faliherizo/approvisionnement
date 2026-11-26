package qc.levis.contrat.clm.application.port;

import qc.levis.contrat.clm.domain.DiffusionM365;

/**
 * Dépôt d'un fichier dans Microsoft 365 pour le module (Annexe 10, TI-01) : bibliothèque SharePoint ou OneDrive, ou canal
 * Teams pour l'avis. L'adaptateur passe par le connecteur Microsoft 365 de la plateforme (ADR-002) : simulé par défaut,
 * Microsoft Graph lorsque l'organisation a configuré et activé sa connexion. Un échec se lit dans le résultat, jamais par
 * exception, pour que la reprise reste décidée par l'application.
 */
public interface DepotMicrosoft365 {

    Resultat deposer(Destination destination, Fichier fichier);

    enum Destination {
        SHAREPOINT, ONEDRIVE, TEAMS;

        public static Destination de(DiffusionM365.Bibliotheque b) {
            return b == DiffusionM365.Bibliotheque.ONEDRIVE ? ONEDRIVE : SHAREPOINT;
        }
    }

    /** {@code dossier} : chemin relatif normalisé dans la bibliothèque ; {@code titre} : métadonnée lisible du dépôt. */
    record Fichier(Long trousseId, String titre, String nom, String mediaType, byte[] octets, String dossier) {
    }

    record Resultat(boolean succes, String url, String message) {
    }
}
