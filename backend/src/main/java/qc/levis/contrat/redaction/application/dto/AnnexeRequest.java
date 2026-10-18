package qc.levis.contrat.redaction.application.dto;

import jakarta.validation.constraints.NotBlank;

// --- Annexes & bordereau (contrats formels) ---
public record AnnexeRequest(String numero, @NotBlank String titre,
                                String referenceClause, String contenu) {}
