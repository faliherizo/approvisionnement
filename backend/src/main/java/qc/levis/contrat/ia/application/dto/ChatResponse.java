package qc.levis.contrat.ia.application.dto;

import java.util.List;

public record ChatResponse(String reponse, String modele, List<String> sources) {}
