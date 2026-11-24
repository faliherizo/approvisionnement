package qc.levis.contrat.interop.application.port;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Dépôt et lecture de fiches pour un échange hérité, par les mêmes cas d'utilisation que l'interface. */
public interface FichesEchange {

    /** Fiche créée et soumise. */
    record FicheDeposee(Long id, String numero, String statut) {
    }

    /** Fiche soumise, telle qu'un système externe la lit. */
    record FicheLue(String numero, String statut, Instant soumisLe, Map<String, Object> donnees) {
        public FicheLue {
            donnees = java.util.Collections.unmodifiableMap(new java.util.LinkedHashMap<>(donnees));
        }
    }

    /** Motif pour lequel les règles du formulaire refuseraient ces données ; vide si elles seraient acceptées. */
    Optional<String> verifier(Long tenantId, Long formulaireId, Map<String, Object> donnees);

    /** Crée et soumet une fiche, dans sa propre transaction : un refus ne laisse aucun brouillon. */
    FicheDeposee deposer(Long tenantId, Long formulaireId, String par, Map<String, Object> donnees);

    /** Fiches soumises (hors brouillons) depuis l'instant donné, ou toutes si {@code depuis} est nul. */
    List<FicheLue> soumises(Long tenantId, Long formulaireId, Instant depuis);
}
