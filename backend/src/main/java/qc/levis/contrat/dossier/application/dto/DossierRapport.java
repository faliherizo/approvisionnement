package qc.levis.contrat.dossier.application.dto;

import qc.levis.contrat.commun.domain.Enums;

/**
* Rapport de suivi d'un dossier (art. 15) : indicateurs agrégés sur les documents
* rattachés — nombre total, répartition par statut, documents approuvés/signés.
*/
public record DossierRapport(
            Long dossierId,
            String titre,
            Enums.StatutDossier statut,
            long nombreDocuments,
            long documentsApprouves,
            long documentsSignes,
            java.util.Map<String, Long> parStatut) {}
