package qc.levis.contrat.vieprivee.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Le motif (référence de la demande écrite reçue, par ex.) est inscrit au registre des demandes. */
public record DemandeEffacementRequest(@NotBlank @Email String courriel, @NotBlank @Size(max = 500) String motif) {}
