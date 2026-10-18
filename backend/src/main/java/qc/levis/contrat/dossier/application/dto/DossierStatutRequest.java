package qc.levis.contrat.dossier.application.dto;

import jakarta.validation.constraints.NotNull;
import qc.levis.contrat.commun.domain.Enums;

/** Changement de statut d'un dossier (cycle de vie). */
public record DossierStatutRequest(@NotNull Enums.StatutDossier statut) {}
