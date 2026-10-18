package qc.levis.contrat.tenant.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Création de l'administrateur d'un tenant (depuis l'espace superadmin). */
public record CreateTenantAdminRequest(
            @Email @NotBlank String email,
            @NotBlank String nom,
            @NotBlank String motDePasse) {}
