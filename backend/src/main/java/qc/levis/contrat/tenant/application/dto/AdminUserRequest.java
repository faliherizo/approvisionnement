package qc.levis.contrat.tenant.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

// --- Gestion des utilisateurs (espace admin d'un tenant) ---
public record AdminUserRequest(
            @Email @NotBlank String email,
            @NotBlank String nom,
            String direction,
            String motDePasse,
            List<String> roles,
            Boolean actif) {}
