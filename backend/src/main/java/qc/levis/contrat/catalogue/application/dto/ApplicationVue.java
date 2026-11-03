package qc.levis.contrat.catalogue.application.dto;

/** Vue d'une application dans le catalogue (écran 2). */
public record ApplicationVue(Long id, String code, String nom, String domaine, String environnement,
                             String statut, String proprietaire, String description, boolean actif) {}
