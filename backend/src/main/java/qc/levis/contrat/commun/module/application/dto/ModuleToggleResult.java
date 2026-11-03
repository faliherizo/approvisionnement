package qc.levis.contrat.commun.module.application.dto;

import java.util.List;

/**
 * Résultat d'une bascule de module : la liste des codes réellement affectés (module + dépendances
 * activées, ou module + dépendants désactivés) et un message explicatif pour l'opérateur.
 */
public record ModuleToggleResult(List<String> codesAffectes, String message) {}
