package qc.levis.contrat.saisie.application.port;

/** Transmet un document à un destinataire (courriel avec pièce jointe) ; rend le statut d'envoi (ENVOYE, SIMULE, ECHEC). */
public interface TransmissionDocument {
    String transmettre(Long tenantId, String destinataire, String sujet, String message, String nomFichier, byte[] contenu);
}
