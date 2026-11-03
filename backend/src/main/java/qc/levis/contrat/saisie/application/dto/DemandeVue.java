package qc.levis.contrat.saisie.application.dto;

import java.time.Instant;

/**
 * Vue d'une demande pour le portail « Mes demandes » : numéro lisible, formulaire et domaine d'origine,
 * statut et dates. Enrichit {@link SaisieVue} du nom et du domaine du formulaire (lecture seule).
 */
public record DemandeVue(Long id, String numero, Long formulaireId, String formulaireNom,
                         String domaine, String statut, Instant majLe, Instant soumisLe) {}
