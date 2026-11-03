package qc.levis.contrat.document.application.dto;

/** Ajout d'un commentaire : {@code visibilite} = INTERNE | DEMANDEUR ; {@code corps} = texte. */
public record CommentaireRequest(String visibilite, String corps) {}
