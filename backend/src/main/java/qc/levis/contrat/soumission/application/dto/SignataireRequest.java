package qc.levis.contrat.soumission.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SignataireRequest(
            @NotBlank String nom,
            @Email @NotBlank String courriel,
            String role) {}
