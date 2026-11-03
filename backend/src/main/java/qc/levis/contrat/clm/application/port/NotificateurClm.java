package qc.levis.contrat.clm.application.port;

/** Envoi d'un message sur un canal (COURRIEL, SMS, INTERNE) ; retourne ENVOYE, SIMULE ou ECHEC. */
public interface NotificateurClm {
    String envoyer(String canal, String destinataire, String sujet, String corps);
}
