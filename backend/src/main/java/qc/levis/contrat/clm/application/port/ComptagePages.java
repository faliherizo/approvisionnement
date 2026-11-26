package qc.levis.contrat.clm.application.port;

import java.util.OptionalInt;
import qc.levis.contrat.clm.domain.FormatDocument;

/**
 * Nombre de pages d'un document (Annexe 10, AF-49). Vide quand le format n'est pas paginé (Word) ou que le fichier ne se lit
 * pas : le comptage ne bloque jamais un dépôt ni une signature.
 */
public interface ComptagePages {

    OptionalInt pages(FormatDocument.Type type, byte[] contenu);
}
