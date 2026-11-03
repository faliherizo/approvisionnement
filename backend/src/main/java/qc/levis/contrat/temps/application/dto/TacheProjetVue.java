package qc.levis.contrat.temps.application.dto;

import java.time.Instant;

/** Vue d'une tâche de projet, enrichie du projet et du nom de l'assigné. */
public record TacheProjetVue(Long id, Long projetId, String projetNom, String nom, String description,
                             String typeTache, String statut, Long assigneId, String assigneNom,
                             Double heuresEstimees, String echeance, Instant creeLe) {}
