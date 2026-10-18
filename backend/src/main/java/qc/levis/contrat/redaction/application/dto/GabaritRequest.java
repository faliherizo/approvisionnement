package qc.levis.contrat.redaction.application.dto;

/** Création / modification d'un gabarit de document normalisé (couverture, corps, design). */
public record GabaritRequest(String code, String nom,
                                 String surLabel, String label, String sousTitre, String piedLabel,
                                 String corps,
                                 String police, String couleurTitre, String couleurAccent, String logoImage,
                                 Boolean afficherCouverture,
                                 Integer margeGauche, Integer margeDroite, Integer margeHaut, Integer margeBas,
                                 Integer ordre, Boolean actif) {}
