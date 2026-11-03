package qc.levis.contrat.notification.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Création/mise à jour d'une règle de notification. */
public record RegleNotifRequest(@NotNull Long formulaireId, @NotBlank String declencheur,
                                @NotBlank String modeleCode, String destinataires) {}
