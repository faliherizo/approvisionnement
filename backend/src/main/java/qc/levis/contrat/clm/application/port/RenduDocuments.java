package qc.levis.contrat.clm.application.port;

import java.util.List;
import qc.levis.contrat.clm.domain.FormatDocument;
import qc.levis.contrat.clm.domain.Modele;

/** Lecture, génération et apposition des signatures sur les documents. */
public interface RenduDocuments {

    record Apposition(String signataire, String horodatage, String reference, Modele.Position position) {
    }

    String texte(FormatDocument.Type type, byte[] contenu);

    byte[] genererPdf(String titre, String texte);

    /** Version Word modifiable, corrigée avant l'approbation finale puis redéposée (addenda 3, Q26). */
    byte[] genererDocx(String titre, String texte);

    /** Appose chaque signature dans sa zone ; sans zone, une page de signatures est ajoutée en fin de document. */
    byte[] apposer(byte[] pdf, List<Apposition> appositions);
}
