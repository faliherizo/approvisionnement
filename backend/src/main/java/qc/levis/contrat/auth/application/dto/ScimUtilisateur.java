package qc.levis.contrat.auth.application.dto;

import java.util.Map;

/**
 * Utilisateur reçu par SCIM 2.0, déjà extrait du message : {@code userName} est le courriel ; {@code actif} nul = inchangé ;
 * {@code attributs} (département, titre…) nul = inchangés (addenda 4, Q31).
 */
public record ScimUtilisateur(String userName, String nom, Boolean actif, Map<String, String> attributs) {

    public ScimUtilisateur(String userName, String nom, Boolean actif) {
        this(userName, nom, actif, null);
    }
}
