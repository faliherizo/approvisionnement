package qc.levis.contrat.vieprivee.application.port;

import qc.levis.contrat.vieprivee.domain.BilanEffacement;
import qc.levis.contrat.vieprivee.domain.ExtraitRenseignements;
import qc.levis.contrat.vieprivee.domain.Pseudonyme;

/**
 * Port qu'implémente chaque module détenant des renseignements personnels : il les extrait et les efface dans son propre
 * périmètre, sans qu'un autre module accède à ses tables. Ajouter une source = ajouter une implémentation (registre).
 */
public interface SourceRenseignementsPersonnels {

    /** Ordre d'effacement : le compte, qui sert à retrouver la personne, s'efface en dernier. */
    default int ordre() { return 0; }

    ExtraitRenseignements extraire(Long tenantId, String courriel);

    BilanEffacement effacer(Long tenantId, String courriel, Pseudonyme pseudonyme);
}
