package qc.levis.contrat.ia.application.dto;

import qc.levis.contrat.commun.domain.Enums;

/** Template recommandé par l'assistant pour le contexte saisi. */
public record TemplateRecommande(Long id, String code, String nom,
                                     Enums.TypeContrat type, Enums.TypeAcquisition categorieAcquisition,
                                     int nombreClauses, boolean correspondanceExacte) {}
