package qc.levis.contrat.auth.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record LoginRequest(
            @Email @NotBlank String email,
            @NotBlank String motDePasse,
            String totpCode,
            List<String> securityAnswers,
            /** Slug du tenant issu du sous-domaine (ex. « levis » pour levis.contrat…). Optionnel. */
            String slug) {}
