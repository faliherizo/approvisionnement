package qc.levis.contrat.securite.application;

import java.util.Set;

/** Identité d'accès d'un utilisateur : ses rôles, ses groupes et son identifiant. */
public record SujetsUtilisateur(Set<String> roles, Set<String> groupes, String utilisateur) {}
