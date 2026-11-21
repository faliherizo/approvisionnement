package qc.levis.contrat.soutien.application.port;

/** Vérifie qu'une organisation existe avant de lui ouvrir une banque d'heures. */
public interface Organisations {
    boolean existe(Long tenantId);
}
