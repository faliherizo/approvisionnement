package qc.levis.contrat.processus.domain;

import java.util.List;

/**
 * Définition d'une étape de workflow (configurable, stockée en JSON — rien de figé).
 * {@code type} : VERIFICATION | APPROBATION | TRAITEMENT (libre). L'affectation vise un rôle ou un groupe.
 * {@code conditions} : l'étape ne s'applique que si TOUTES les conditions sont vraies (routage par montant,
 * type, etc.). {@code commentaireRefusObligatoire} impose un motif au refus (écran 5).
 */
public record EtapeDef(String code, String nom, String type, String responsableRole, String responsableGroupe,
                       Integer delaiJours, boolean commentaireRefusObligatoire, List<ConditionEtape> conditions) {
    public List<ConditionEtape> conditions() {
        return conditions == null ? List.of() : conditions;
    }
}
