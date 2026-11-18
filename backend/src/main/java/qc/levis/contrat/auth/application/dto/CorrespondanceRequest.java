package qc.levis.contrat.auth.application.dto;

/** Correspondance d'annuaire à définir : {@code source} GROUPE ou ATTRIBUT ; un rôle, un domaine d'affaires, ou les deux. */
public record CorrespondanceRequest(String source, String attribut, String valeur, String role, String groupe) {
}
