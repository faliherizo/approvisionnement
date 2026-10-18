package qc.levis.contrat.tenant.application.dto;

import jakarta.validation.constraints.NotBlank;

public record TenantRequest(
            @NotBlank String slug,
            @NotBlank String titre,
            String sousTitre) {}
