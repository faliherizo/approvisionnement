package qc.levis.contrat.rh.application.dto;

/** Vue d'un dossier employé. Le NAS est masqué (jamais relu en clair — Loi 25). */
public record EmployeVue(Long id, String matricule, String prenom, String nom, String nasMasque, String courriel,
                         String telephone, String dateEmbauche, String dateFin, String statut, String poste,
                         String departement, double salaireAnnuel, String frequence, String province,
                         String typeTravailleur, boolean actif) {}
