package qc.levis.contrat.temps.application.dto;

import java.time.Instant;

/** Vue d'une feuille de temps. */
public record FeuilleTempsVue(Long id, String proprietaire, Long employeId, String periodeDebut, String periodeFin,
                              String statut, String lignesJson, double totalHeures, String approbateur,
                              String commentaire, Instant majLe) {}
