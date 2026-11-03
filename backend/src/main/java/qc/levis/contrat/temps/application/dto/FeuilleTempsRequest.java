package qc.levis.contrat.temps.application.dto;

import java.util.List;
import java.util.Map;

/** Création/mise à jour d'une feuille de temps : période + lignes [{date, categorie, heures}]. */
public record FeuilleTempsRequest(String periodeDebut, String periodeFin, List<Map<String, Object>> lignes) {}
