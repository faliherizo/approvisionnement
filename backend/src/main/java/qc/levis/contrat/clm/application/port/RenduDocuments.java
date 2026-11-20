package qc.levis.contrat.clm.application.port;

import java.util.List;
import java.util.Map;
import qc.levis.contrat.clm.domain.FormatDocument;
import qc.levis.contrat.clm.domain.Modele;

/** Lecture, génération et apposition des signatures sur les documents. */
public interface RenduDocuments {

    record Apposition(String signataire, String horodatage, String reference, Modele.Position position) {
    }

    /** Clause retenue, variables déjà remplacées, insérée au marqueur {@code {{clauses}}} d'un gabarit Word. */
    record ClauseFusionnee(String titre, String texte) {
    }

    String texte(FormatDocument.Type type, byte[] contenu);

    /**
     * Gabarit Word de l'organisation (addenda 4, Q22) : variables remplacées dans le corps, les tableaux, les en-têtes
     * et les pieds de page, clauses insérées au marqueur ; styles, tableaux, images et sections restent intacts.
     */
    byte[] fusionnerDocx(byte[] gabarit, Map<String, Object> valeurs, List<ClauseFusionnee> clauses);

    byte[] genererPdf(String titre, String texte);

    /** Version Word modifiable, corrigée avant l'approbation finale puis redéposée (addenda 3, Q26). */
    byte[] genererDocx(String titre, String texte);

    /** Appose chaque signature dans sa zone ; sans zone, une page de signatures est ajoutée en fin de document. */
    byte[] apposer(byte[] pdf, List<Apposition> appositions);
}
