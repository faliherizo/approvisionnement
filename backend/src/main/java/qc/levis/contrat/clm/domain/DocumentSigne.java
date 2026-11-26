package qc.levis.contrat.clm.domain;

import java.time.Instant;

/**
 * Version signée d'un document, telle que le relevé d'utilisation la compte (Annexe 10, AF-49) : l'unité de sa trousse,
 * l'instant où la trousse s'est terminée et le nombre de pages, nul s'il n'a pas pu être compté.
 */
public record DocumentSigne(Long documentId, String unite, Instant signeLe, Integer pages) {
}
