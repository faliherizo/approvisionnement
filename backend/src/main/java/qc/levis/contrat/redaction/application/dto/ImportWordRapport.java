package qc.levis.contrat.redaction.application.dto;

import java.util.List;

/** Rapport d'import d'un modèle Word (.docx) : sections détectées, clauses créées, modèle. */
public record ImportWordRapport(boolean essai, int sectionsDetectees, int clausesCreees,
                                    Long templateId, String templateNom, List<LigneErreur> erreurs) {}
