package qc.levis.contrat.soutien.application.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import qc.levis.contrat.soutien.domain.CategorieService;
import qc.levis.contrat.soutien.domain.Mouvement;

/** Achat, consommation (catégorie et intervenant requis) ou correction d'heures. */
public record MouvementRequest(@NotNull Mouvement.Type type, CategorieService categorie,
                               @NotNull @Positive @Digits(integer = 6, fraction = 2) BigDecimal heures, @NotNull LocalDate date,
                               @NotBlank @Size(max = 500) String description, @Size(max = 150) String intervenant) {}
