package qc.levis.contrat.document.application.dto;

import java.time.Instant;

/** Vue d'un document (sans le binaire). Le {@code contenu} n'est jamais exposé dans une liste. */
public record DocumentVue(Long id, Long saisieId, String nomOriginal, String mediaType, long tailleOctets,
                          String classification, String uploadePar, Instant uploadeLe) {}
