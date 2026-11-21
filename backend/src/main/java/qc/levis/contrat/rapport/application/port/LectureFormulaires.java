package qc.levis.contrat.rapport.application.port;

import java.util.Map;
import java.util.Optional;

/** Lecture, par le module de rapports, du nom et des libellés de questions d'un formulaire de l'organisation. */
public interface LectureFormulaires {

    /** Nom du formulaire et libellés de ses questions (clé → libellé), dans l'ordre du formulaire. */
    record Description(String nom, Map<String, String> libelles) {
        public Description {
            libelles = java.util.Collections.unmodifiableMap(new java.util.LinkedHashMap<>(libelles));
        }
    }

    Optional<Description> decrire(Long tenantId, Long formulaireId);
}
