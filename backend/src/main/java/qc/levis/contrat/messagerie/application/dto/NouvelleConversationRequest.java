package qc.levis.contrat.messagerie.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record NouvelleConversationRequest(@NotBlank @Size(max = 200) String sujet, @Size(max = 100) String reference,
                                          @NotEmpty @Size(max = 19) List<@NotBlank @Email String> participants,
                                          @NotBlank @Size(max = 5000) String message) {}
