package qc.levis.contrat.auth.application.dto;

import java.util.List;

public record MeResponse(String email, String nom, String direction, List<String> roles,
                             boolean mfaActive, String mfaMethode) {}
