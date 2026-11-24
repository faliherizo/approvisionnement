package qc.levis.contrat.interop.application.dto;

import java.time.Instant;
import java.util.Map;

/** Échanges hérités (CHU-8.04) : vues et demandes de l'API. */
public final class EchangeDto {

    private EchangeDto() {
    }

    /** Déclaration d'un échange : son code, le formulaire cible (par code) et la table « ancien nom → clé de champ ». */
    public record Declaration(String code, String formulaireCode, Map<String, String> correspondances) {
    }

    public record Vue(Long id, String code, Long formulaireId, String formulaireCode, String formulaireNom,
                      Map<String, String> correspondances, boolean actif, String creePar, Instant creeLe) {
    }

    /** Fiche lue sous les anciens noms. */
    public record FicheHeritee(String numero, String statut, Instant soumisLe, Map<String, Object> donnees) {
    }
}
