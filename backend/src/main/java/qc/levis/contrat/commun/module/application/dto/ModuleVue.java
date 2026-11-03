package qc.levis.contrat.commun.module.application.dto;

import java.util.List;

/** Vue d'un module et de son état d'activation pour un tenant. */
public record ModuleVue(String code, String nom, String famille, boolean actif, List<String> requiert) {}
