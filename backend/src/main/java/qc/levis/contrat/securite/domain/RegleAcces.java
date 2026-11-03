package qc.levis.contrat.securite.domain;

/**
 * Règle d'accès granulaire (forme pure) : accorde {@code action} sur {@code cible} de type {@code portee}
 * au sujet ({@code sujetType} ∈ ROLE/GROUPE/UTILISATEUR, valeur {@code sujet}). « * » = joker.
 * Portées : FORMULAIRE/CHAMP/PAGE/FICHE/BOUTON. Actions : LIRE/MODIFIER/SUPPRIMER.
 */
public record RegleAcces(String sujetType, String sujet, String portee, String cible, String action) {}
