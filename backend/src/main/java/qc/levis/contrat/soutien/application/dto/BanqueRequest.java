package qc.levis.contrat.soutien.application.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Ouverture d'une banque d'heures avec son achat initial. */
public record BanqueRequest(@NotBlank @Size(max = 150) String nom, @NotNull LocalDate debut, @NotNull LocalDate fin,
                            @NotNull @Positive @Digits(integer = 6, fraction = 2) BigDecimal heures) {}
