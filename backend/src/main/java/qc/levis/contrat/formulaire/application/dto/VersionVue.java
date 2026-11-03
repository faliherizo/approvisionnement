package qc.levis.contrat.formulaire.application.dto;

import java.time.Instant;

/** Vue d'une version de formulaire (structure incluse). */
public record VersionVue(int numero, String statut, String structureJson, String note, Instant publieLe) {}
