package qc.levis.contrat.clm.application.port;

/**
 * Cachet électronique normalisé des preuves (Cahier § 1.4.11 : standards reconnus de l'industrie). Signature CMS
 * détachée (CAdES, RFC 5652) d'un document final, et signature intégrée (PAdES) du certificat de signature ; toutes deux
 * vérifiables par des outils indépendants. L'horodatage RFC 3161 s'ajoute quand une autorité est configurée.
 */
public interface CachetElectronique {

    /** Signature CMS détachée, encodée DER (fichier {@code .p7s}). */
    byte[] signerDetache(byte[] contenu);

    /** PDF identique, augmenté d'une signature intégrée qui couvre tout le document. */
    byte[] scellerPdf(byte[] pdf, String motif);

    /** Titulaire du certificat de cachet (nom distinctif X.500), cité dans le certificat de signature. */
    String titulaire();

    /** Vrai pour le certificat de démonstration généré au démarrage, à remplacer par celui d'une autorité reconnue. */
    boolean demonstration();
}
