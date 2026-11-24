package qc.levis.contrat.interop.application.port;

import java.util.Optional;
import java.util.Set;

/** Lecture, par le module d'interopérabilité, des formulaires de l'organisation qui reçoivent un échange hérité. */
public interface FormulairesEchange {

    /** Formulaire cible : identifiant, code, nom et clés de ses champs (dernière version). */
    record FormulaireCible(Long id, String code, String nom, Set<String> cles) {
        public FormulaireCible {
            cles = Set.copyOf(cles);
        }
    }

    Optional<FormulaireCible> parCode(Long tenantId, String code);

    Optional<FormulaireCible> parId(Long tenantId, Long formulaireId);
}
