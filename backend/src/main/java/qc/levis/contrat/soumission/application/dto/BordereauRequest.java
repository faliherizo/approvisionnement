package qc.levis.contrat.soumission.application.dto;

import java.util.List;

public record BordereauRequest(String titre, List<LigneRequest> lignes) {}
