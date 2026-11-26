package qc.levis.contrat.clm.application;

import java.util.Map;
import qc.levis.contrat.clm.domain.Trousse;

/**
 * Fichiers qu'une fonction optionnelle du module verse au dossier de preuve d'une trousse (ex. l'historique d'une négociation,
 * RST-55). Ils entrent au manifeste et sont donc couverts par le sceau de l'archive.
 */
public interface PieceDePreuve {

    /** Chemin dans l'archive → contenu ; vide si la trousse n'a rien à verser. */
    Map<String, byte[]> pieces(Trousse t);
}
