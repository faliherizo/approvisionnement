package qc.levis.contrat.auth.application.dto;

/** Mise à jour du profil personnel (self-service). */
public record ProfileUpdateRequest(String nom, String direction,
                                       String motDePasseActuel, String nouveauMotDePasse) {}
