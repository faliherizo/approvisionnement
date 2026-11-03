package qc.levis.contrat.rh.application.dto;

import jakarta.validation.constraints.NotBlank;

/** Création / mise à jour d'un dossier employé. */
public record EmployeRequest(@NotBlank String prenom, @NotBlank String nom, String nas, String courriel,
                             String telephone, String dateEmbauche, String dateFin, String statut, String poste,
                             String departement, double salaireAnnuel, String frequence, String province,
                             String typeTravailleur) {}
