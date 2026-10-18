package qc.levis.contrat.ia.application.dto;

import jakarta.validation.constraints.NotBlank;

/** Base de connaissances IA (RAG). */
public record KnowledgeRequest(@NotBlank String titre, String source, @NotBlank String contenu) {}
