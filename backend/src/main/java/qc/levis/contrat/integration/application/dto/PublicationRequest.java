package qc.levis.contrat.integration.application.dto;

import jakarta.validation.constraints.NotNull;
import qc.levis.contrat.commun.domain.Enums;

/** Demande de publication d'un document vers une cible externe. */
public record PublicationRequest(
            @NotNull Enums.CiblePublication cible,
            Enums.FormatPublication format,
            String gabarit,
            String destination) {}
