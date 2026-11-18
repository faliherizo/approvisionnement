package qc.levis.contrat.auth.application.dto;

/** Utilisateur reçu par SCIM 2.0, déjà extrait du message : {@code userName} est le courriel ; {@code actif} nul = inchangé. */
public record ScimUtilisateur(String userName, String nom, Boolean actif) {
}
