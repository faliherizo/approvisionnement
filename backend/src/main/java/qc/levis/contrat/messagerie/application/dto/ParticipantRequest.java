package qc.levis.contrat.messagerie.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ParticipantRequest(@NotBlank @Email String courriel) {}
