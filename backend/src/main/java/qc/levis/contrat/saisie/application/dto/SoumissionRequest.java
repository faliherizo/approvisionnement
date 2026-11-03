package qc.levis.contrat.saisie.application.dto;

/** Soumission d'une fiche : données (JSON clés-par-code) + jeton captcha éventuel. */
public record SoumissionRequest(String donneesJson, String captchaToken) {}
