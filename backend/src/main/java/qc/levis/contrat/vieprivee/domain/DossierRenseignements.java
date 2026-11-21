package qc.levis.contrat.vieprivee.domain;

import java.time.Instant;
import java.util.List;

/** Réponse à une demande d'accès (Loi 25, art. 27) : tout ce que l'organisation détient sur la personne, par source. */
public record DossierRenseignements(String courriel, Instant produitLe, List<ExtraitRenseignements> sources) {
    public DossierRenseignements {
        sources = List.copyOf(sources);
    }

    public int total() {
        return sources.stream().mapToInt(s -> s.elements().size()).sum();
    }
}
