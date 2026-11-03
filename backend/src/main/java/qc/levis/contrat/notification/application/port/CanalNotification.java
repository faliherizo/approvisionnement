package qc.levis.contrat.notification.application.port;

/** Port d'envoi par canal (COURRIEL/SMS/INTERNE). Adaptateurs réels branchés en infrastructure. */
public interface CanalNotification {
    /** Code du canal géré (COURRIEL, SMS, INTERNE). */
    String canal();
    /** Envoie le message ; retourne le statut (ENVOYE, SIMULE, ECHEC). */
    String envoyer(String destinataire, String sujet, String corps);
}
