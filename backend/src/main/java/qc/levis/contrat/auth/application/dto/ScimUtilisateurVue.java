package qc.levis.contrat.auth.application.dto;

import java.util.List;

public record ScimUtilisateurVue(String id, String userName, String nom, boolean actif, List<String> groupes) {
}
