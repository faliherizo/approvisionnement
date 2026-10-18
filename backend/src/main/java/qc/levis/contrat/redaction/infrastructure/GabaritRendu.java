package qc.levis.contrat.redaction.infrastructure;

import qc.levis.contrat.redaction.domain.CorpsGabarit;
import qc.levis.contrat.redaction.domain.Gabarit;

/**
 * Valeur immuable décrivant la présentation d'un gabarit au moteur de rendu :
 * libellés de couverture / pied, mode de corps et marges. Produite par le service
 * de gabarits à partir de l'entité persistée, elle découple le rendu du modèle de
 * données (le port {@link DocumentRenderer} reste inchangé).
 */
public record GabaritRendu(
        String surLabel,
        String label,
        String sousTitre,
        String piedLabel,
        CorpsGabarit corps,
        int margeGauche,
        int margeDroite,
        int margeHaut,
        int margeBas) {

    public boolean aSousTitre() {
        return sousTitre != null && !sousTitre.isBlank();
    }

    /** Gabarit de rendu par défaut (rétro-compatible avec l'export « contrat » historique). */
    public static GabaritRendu defaut() {
        return new GabaritRendu("APPEL D'OFFRES - SERVICES", "CONTRAT",
                "Services de nature technique", "CONTRAT", CorpsGabarit.CONTRAT, 56, 56, 64, 54);
    }
}
