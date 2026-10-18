package qc.levis.contrat.redaction.application.dto;

import java.util.List;

/** Rapport d'import (mode essai ou réel) : compteurs et erreurs par ligne. */
public record ImportRapport(boolean essai, int lignesLues, int valides, int importes,
                                List<LigneErreur> erreurs) {}
