package qc.levis.contrat.auth.application.dto;

import java.util.List;

public record ScimGroupeVue(String id, String nom, String idExterne, List<String> membres) {
}
