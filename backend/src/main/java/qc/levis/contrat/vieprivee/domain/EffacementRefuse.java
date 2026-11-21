package qc.levis.contrat.vieprivee.domain;

/** L'effacement demandé laisserait l'organisation dans un état interdit (ex. sans administrateur, ou s'effacer soi-même). */
public class EffacementRefuse extends IllegalStateException {
    public EffacementRefuse(String message) {
        super(message);
    }
}
