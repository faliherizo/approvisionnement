package qc.levis.contrat.document.application.dto;

import java.time.Instant;

/** Vue d'un commentaire de demande. */
public record CommentaireVue(Long id, Long saisieId, String visibilite, String corps, String auteur, Instant creeLe) {}
