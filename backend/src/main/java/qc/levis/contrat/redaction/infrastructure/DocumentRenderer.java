package qc.levis.contrat.redaction.infrastructure;

/**
 * Port de rendu d'un contrat (architecture hexagonale). Chaque adaptateur cible un
 * format (PDF, DOCX). Permet d'ajouter de nouveaux formats sans modifier les appelants (OCP).
 */
public interface DocumentRenderer {

    enum Format { PDF, DOCX }

    Format format();

    byte[] rendre(ContractModel model, Branding branding);
}
