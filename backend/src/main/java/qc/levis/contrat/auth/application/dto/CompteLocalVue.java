package qc.levis.contrat.auth.application.dto;

/** Compte de l'organisation vu par la politique de connexion locale. */
public record CompteLocalVue(Long id, String courriel, String nom, boolean secondFacteur, boolean secours) {
}
