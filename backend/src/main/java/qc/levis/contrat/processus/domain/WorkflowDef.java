package qc.levis.contrat.processus.domain;

import java.util.List;

/** Définition d'un workflow : séquence ordonnée d'étapes (certaines conditionnelles). Stockée en JSON. */
public record WorkflowDef(List<EtapeDef> etapes) {
    public List<EtapeDef> etapes() {
        return etapes == null ? List.of() : etapes;
    }
}
