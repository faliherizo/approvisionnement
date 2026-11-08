package qc.levis.contrat.reference.application.dto;

/**
 * Valeur d'une liste telle que la gère l'administration. {@code origine} : PLATEFORME (valeur par défaut, non modifiée
 * par l'organisation), PERSONNALISEE (valeur de la plateforme remplacée par l'organisation) ou ORGANISATION (propre à
 * l'organisation).
 */
public record ValeurReference(Long id, String categorie, String code, String libelle, String detail, int ordre, boolean actif,
                              String origine) {
}
