package qc.levis.contrat.commun.reversibilite.application;

import java.util.List;
import java.util.Map;

/**
 * Contribution d'un module à l'export de réversibilité (Lévis art. 20, CHU 6.06) : chaque module livre ses propres jeux de
 * données, sans que l'export connaisse ses entités. Un nouveau module s'ajoute par une nouvelle implémentation, sans
 * modifier l'export.
 */
public interface SourceReversibilite {

    /** Module dont relèvent les données ; elles ne sont exportées que si l'organisation l'a souscrit. */
    String module();

    /** Jeux de données de l'organisation, par nom de fichier (ex. « formulaires.json »), en types sérialisables. */
    Map<String, List<?>> jeuxDeDonnees(Long tenant);
}
