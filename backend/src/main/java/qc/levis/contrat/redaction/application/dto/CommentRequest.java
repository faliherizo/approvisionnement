package qc.levis.contrat.redaction.application.dto;

import jakarta.validation.constraints.NotBlank;

public record CommentRequest(@NotBlank String corps, Long clauseId, Long parentId) {}
