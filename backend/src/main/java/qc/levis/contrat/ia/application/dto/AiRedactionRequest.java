package qc.levis.contrat.ia.application.dto;

import jakarta.validation.constraints.NotBlank;

/** Assistance IA (Ollama) - rédaction de clause. */
public record AiRedactionRequest(
            @NotBlank String instruction,
            String contexte,
            String texteExistant) {}
