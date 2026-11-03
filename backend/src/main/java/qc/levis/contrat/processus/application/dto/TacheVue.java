package qc.levis.contrat.processus.application.dto;

import java.time.Instant;

/** Vue d'une tâche assignée (écran 7 « Tâches et approbations »), enrichie du n° de demande et du formulaire. */
public record TacheVue(Long id, Long saisieId, String numeroDemande, Long formulaireId, String formulaireNom,
                       String domaine, String etapeCode, String etapeNom, String statut,
                       boolean commentaireRefusObligatoire, Instant echeanceLe, Instant creeLe) {}
