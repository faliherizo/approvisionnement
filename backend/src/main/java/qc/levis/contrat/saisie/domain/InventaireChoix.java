package qc.levis.contrat.saisie.domain;

import java.time.Instant;
import java.util.List;

/** Inventaire d'un choix (CHU 4.08) : ses places et les inscriptions qui les occupent. */
public record InventaireChoix(PlaceChoix place, List<Inscription> inscriptions) {

    public InventaireChoix {
        inscriptions = List.copyOf(inscriptions);
    }

    /** Une fiche qui occupe une place du choix. */
    public record Inscription(String numero, String statut, Instant soumisLe, String demandeur) {}
}
