package qc.levis.contrat.vieprivee.domain;

import java.util.List;

/**
 * Ce qu'une source détient sur une personne (Loi 25, art. 27) : la nature des renseignements, leur usage, ce que deviendrait
 * leur effacement, et les renseignements eux-mêmes.
 */
public record ExtraitRenseignements(String source, String description, String effacement, List<Renseignement> elements) {
    public ExtraitRenseignements {
        elements = List.copyOf(elements);
    }
}
