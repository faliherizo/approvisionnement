package qc.levis.contrat.clm.application.port;

import java.util.Optional;
import qc.levis.contrat.clm.domain.PositionNegociation;

/**
 * Conseil d'IA sur une proposition de la contrepartie (RST-55, ADR-038) : résumé des écarts et position recommandée au regard
 * de la clause normative. Vide si l'IA est indisponible, désactivée pour l'organisation ou sans réponse exploitable : le cas
 * d'utilisation retombe alors sur les règles déterministes. Le conseil n'est jamais appliqué sans décision humaine.
 */
public interface ConseillerNegociation {

    Optional<Avis> conseiller(Demande demande);

    /** Clause visée, son texte normatif (bibliothèque du modèle), le texte en vigueur et la proposition commentée. */
    record Demande(String clause, String texteNormatif, String texteActuel, String textePropose, String commentaire) {
    }

    /** Résumé rédigé par le modèle, position qu'il nomme (s'il en nomme une) et nom du modèle, pour la traçabilité. */
    record Avis(String resume, Optional<PositionNegociation> position, String modele) {
    }
}
