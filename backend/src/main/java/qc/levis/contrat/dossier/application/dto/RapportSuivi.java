package qc.levis.contrat.dossier.application.dto;

import java.util.List;

/** Tableau de bord de suivi : portefeuille de documents + éléments d'action + activité. */
public record RapportSuivi(
            long totalDocuments,
            java.util.Map<String, Long> documentsParStatut,
            java.util.Map<String, Long> documentsParType,
            long approbationsEnAttente,
            long dossiersActifs,
            long obligationsAVenir,
            List<ActiviteUtilisateur> activiteParUtilisateur) {}
