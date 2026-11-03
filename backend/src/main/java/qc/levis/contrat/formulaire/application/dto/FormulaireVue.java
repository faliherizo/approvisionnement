package qc.levis.contrat.formulaire.application.dto;

/** Vue synthétique d'un formulaire et de sa version courante. */
public record FormulaireVue(Long id, String code, String nom, String domaine, Long parentId, boolean actif,
                            Integer versionCourante, String statutCourant, Integer conservationJours) {}
