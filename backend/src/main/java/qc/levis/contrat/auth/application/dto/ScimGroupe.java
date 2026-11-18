package qc.levis.contrat.auth.application.dto;

import java.util.List;

/** Groupe reçu par SCIM 2.0 ; {@code membres} : identifiants SCIM des utilisateurs, nul = inchangé. */
public record ScimGroupe(String nom, String idExterne, List<String> membres) {
}
