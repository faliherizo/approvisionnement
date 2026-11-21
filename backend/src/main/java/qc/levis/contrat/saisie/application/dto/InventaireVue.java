package qc.levis.contrat.saisie.application.dto;

import java.util.List;
import qc.levis.contrat.saisie.domain.InventaireChoix;

/** Inventaire d'un choix : ses places et les inscriptions qui les occupent. */
public record InventaireVue(PlaceVue place, List<InventaireChoix.Inscription> inscriptions) {
    public static InventaireVue de(InventaireChoix i) {
        return new InventaireVue(PlaceVue.de(i.place()), i.inscriptions());
    }
}
