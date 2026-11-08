package qc.levis.contrat.clm.application.port;

import java.util.List;

/** Personnes actives de l'organisation, à qui une tâche peut être confiée ; fourni par le contexte des comptes. */
public interface AnnuaireOrganisation {

    record Membre(String courriel, String nom) {
    }

    List<Membre> membres(Long tenantId);

    default boolean estMembre(Long tenantId, String courriel) {
        return courriel != null && membres(tenantId).stream().anyMatch(m -> m.courriel().equalsIgnoreCase(courriel.trim()));
    }
}
