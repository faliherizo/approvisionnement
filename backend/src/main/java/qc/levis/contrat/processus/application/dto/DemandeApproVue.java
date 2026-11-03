package qc.levis.contrat.processus.application.dto;

import java.time.Instant;
import java.util.List;

/** Vue d'une demande d'approbation et de ses étapes. */
public record DemandeApproVue(Long id, String documentType, String documentId, String demandeur, String statut,
                              Instant creeLe, List<EtapeApproVue> etapes) {}
