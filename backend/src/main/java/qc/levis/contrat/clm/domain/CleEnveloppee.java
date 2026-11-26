package qc.levis.contrat.clm.domain;

/**
 * Clé de données d'une organisation enveloppée par une clé maîtresse (ADR-037) : seul le service de gestion des clés (clé
 * locale ou Azure Key Vault) sait l'ouvrir. La clé maîtresse est désignée par son identifiant (« locale-1 », ou l'URL
 * versionnée d'une clé Key Vault), jamais par sa valeur.
 *
 * @param cleMaitresse identifiant de la clé maîtresse qui l'a enveloppée
 * @param valeur       clé de données enveloppée, en Base64
 */
public record CleEnveloppee(String cleMaitresse, String valeur) {

    public static final int CLE_MAITRESSE_MAX = 300;
    public static final int VALEUR_MAX = 2048;

    public CleEnveloppee {
        if (cleMaitresse == null || cleMaitresse.isBlank() || cleMaitresse.length() > CLE_MAITRESSE_MAX) {
            throw new IllegalArgumentException("Identifiant de clé maîtresse invalide.");
        }
        if (valeur == null || valeur.isBlank() || valeur.length() > VALEUR_MAX) {
            throw new IllegalArgumentException("Clé enveloppée invalide.");
        }
    }
}
