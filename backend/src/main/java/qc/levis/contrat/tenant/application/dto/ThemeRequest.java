package qc.levis.contrat.tenant.application.dto;

/** Thème / design d'un tenant (personnalisation superadmin). */
public record ThemeRequest(String couleurPrimaire, String couleurAccent,
                               String couleurFond, String couleurTexte,
                               String logoTexte, String logoImage) {}
