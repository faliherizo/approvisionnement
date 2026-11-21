package qc.levis.contrat.rapport.application.dto;

import java.time.Instant;
import java.util.List;
import qc.levis.contrat.rapport.domain.Agregation;
import qc.levis.contrat.rapport.domain.Indicateur;

/** Tableau de bord tel que l'interface l'affiche : sa configuration et, si demandées, les valeurs de chaque indicateur. */
public record TableauBordVue(Long id, String nom, String proprietaire, boolean partage, boolean modifiable, Instant modifieLe,
                             List<IndicateurVue> indicateurs) {
    public record IndicateurVue(Indicateur indicateur, List<Agregation.Point> points) {}
}
