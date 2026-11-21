package qc.levis.contrat.saisie.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TransmissionRequest(@NotBlank @Email String destinataire, @Size(max = 2000) String message) {}
