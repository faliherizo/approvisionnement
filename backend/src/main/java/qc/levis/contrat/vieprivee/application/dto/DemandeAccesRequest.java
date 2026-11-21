package qc.levis.contrat.vieprivee.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Le courriel voyage dans le corps de la requête, jamais dans l'adresse : il n'apparaît pas dans les journaux d'accès. */
public record DemandeAccesRequest(@NotBlank @Email String courriel) {}
