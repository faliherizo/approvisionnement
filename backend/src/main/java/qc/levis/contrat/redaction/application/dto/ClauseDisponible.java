package qc.levis.contrat.redaction.application.dto;

/**
* Clause proposée à l'assemblage : toutes les clauses actives sont exposées,
* avec le verdict du moteur de règles ({@code eligible}) et l'auto-inclusion
* ({@code autoInclus} = clause STANDARD éligible, intégrée sans sélection).
*/
public record ClauseDisponible(Long id, String code, String titre, String categorie,
                                   boolean normative, boolean eligible, boolean autoInclus) {}
