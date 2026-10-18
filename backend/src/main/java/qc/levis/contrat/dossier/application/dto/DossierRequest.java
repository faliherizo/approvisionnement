package qc.levis.contrat.dossier.application.dto;

import jakarta.validation.constraints.NotBlank;
import qc.levis.contrat.commun.domain.Enums;

/** Création / mise à jour d'un dossier d'approvisionnement. */
public record DossierRequest(
            @NotBlank String titre,
            String numeroAO,
            String objet,
            Enums.TypeAcquisition typeAcquisition,
            Enums.ModeSollicitation modeSollicitation,
            String responsable) {}
