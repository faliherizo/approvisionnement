package qc.levis.contrat.processus.application.dto;

import java.time.Instant;

/** Vue d'une étape d'approbation (enrichie du type/document de la demande). */
public record EtapeApproVue(Long id, Long demandeId, String documentType, String documentId, int niveau,
                            String approbateurRole, String approbateurId, String statut, String decidePar,
                            String commentaire, Instant decideLe) {}
