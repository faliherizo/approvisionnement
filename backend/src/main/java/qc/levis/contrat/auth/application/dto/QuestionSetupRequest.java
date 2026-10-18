package qc.levis.contrat.auth.application.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record QuestionSetupRequest(@NotNull List<QuestionItem> questions) {}
