package qc.levis.contrat.clm.application.port;

import java.util.Set;

/** Groupes et unités administratives d'un utilisateur (ST-02), fournis par le contexte sécurité. */
public interface PerimetreUtilisateur {
    Set<String> groupes(Long tenantId, String utilisateur);
}
