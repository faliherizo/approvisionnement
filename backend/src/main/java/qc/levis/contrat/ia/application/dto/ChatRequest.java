package qc.levis.contrat.ia.application.dto;

import jakarta.validation.constraints.NotBlank;

/** Message de chat vers l'assistant IA spécialisé. */
public record ChatRequest(@NotBlank String question, String contexteDocument) {}
