package qc.levis.contrat.tenant.application.dto;

import java.util.List;

public record PageResponse<T>(List<T> contenu, long total, int page, int taille) {}
