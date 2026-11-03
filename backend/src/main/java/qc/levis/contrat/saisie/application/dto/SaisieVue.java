package qc.levis.contrat.saisie.application.dto;

import java.time.Instant;

/** Vue d'une fiche (soumission). */
public record SaisieVue(Long id, Long formulaireId, String numero, int versionNumero, String statut,
                        String donneesJson, Instant majLe, Instant soumisLe) {}
