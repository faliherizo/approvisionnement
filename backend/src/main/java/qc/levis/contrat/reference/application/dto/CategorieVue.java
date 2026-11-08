package qc.levis.contrat.reference.application.dto;

/** Liste de référence offerte à la gestion ; {@code libelleDetail} nul : la liste n'a pas de complément par valeur. */
public record CategorieVue(String code, String module, String libelle, String aide, String libelleDetail) {
}
