package qc.levis.contrat.approbation.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import qc.levis.contrat.commun.domain.Enums;

/** Gabarit de circuit d'approbation : rôles requis dans l'ordre, par type de document. */
public record WorkflowRequest(
            @NotBlank String nom,
            @NotNull Enums.TypeContrat type,
            @NotNull List<Enums.Role> etapes,
            Boolean actif) {}
