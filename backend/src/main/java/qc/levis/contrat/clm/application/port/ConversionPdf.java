package qc.levis.contrat.clm.application.port;

/**
 * Conversion d'un document Word en PDF/A fidèle à sa mise en page (addenda 4, Q22), côté serveur. L'implémentation
 * dépend du déploiement (LibreOffice sans interface par défaut) ; {@link #disponible()} dit si elle est en service.
 */
public interface ConversionPdf {

    boolean disponible();

    /** PDF/A-2b du document Word ; échoue par {@link IllegalStateException} si la conversion n'aboutit pas. */
    byte[] versPdf(byte[] docx);
}
