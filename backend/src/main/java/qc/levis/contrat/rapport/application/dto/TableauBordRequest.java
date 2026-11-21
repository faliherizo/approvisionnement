package qc.levis.contrat.rapport.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import qc.levis.contrat.rapport.domain.Indicateur;

public record TableauBordRequest(@NotBlank @Size(max = 120) String nom, boolean partage,
                                 @NotNull @Size(max = 12) List<@Valid IndicateurRequest> indicateurs) {

    public record IndicateurRequest(@NotBlank @Size(max = 100) String titre, @NotNull Indicateur.Type type, @NotNull Long formulaireId,
                                    Indicateur.Regroupement regroupement, @Size(max = 100) String champ, List<String> statuts) {
        public Indicateur versDomaine() {
            return new Indicateur(titre, type, formulaireId, regroupement, champ, statuts);
        }
    }
}
