package qc.levis.contrat.redaction.application.dto;

/** Modèle de mise en forme des documents générés (gabarit par entreprise). */
public record ModeleDocumentRequest(String police, String couleurTitre, String couleurAccent,
                                        String marque, String piedDePage, Boolean afficherCouverture,
                                        String logoImage) {}
