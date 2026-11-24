package qc.levis.contrat.commun.antivirus.application;

/**
 * Analyse antivirus d'un fichier reçu de l'extérieur (CHU 7.08, UI-05 d'IQ). L'adaptateur est choisi par configuration :
 * signatures de test par défaut, ClamAV en exploitation (ADR-027).
 */
public interface Antivirus {

    /**
     * @return vrai si aucun maliciel n'est détecté
     * @throws AntivirusIndisponible si l'analyse n'a pas pu être menée : le fichier n'est alors jamais accepté
     */
    boolean estSain(byte[] contenu);

    /** L'analyse n'a pas pu être menée (service injoignable, réponse inattendue). */
    class AntivirusIndisponible extends RuntimeException {
        public AntivirusIndisponible(String message) {
            super(message);
        }
    }

    /** Un fichier où un maliciel est détecté est refusé, avec un message que l'utilisateur peut comprendre. */
    class FichierInfecte extends IllegalArgumentException {
        public FichierInfecte(String nom) {
            super("Fichier refusé : l'analyse antivirus a détecté un maliciel dans « " + nom + " ».");
        }
    }
}
