package qc.levis.contrat.commun.application;

/**
 * Utilitaires de journalisation sûre : masque les identifiants à caractère personnel avant qu'ils
 * n'apparaissent dans les journaux techniques (Loi 25). L'identité complète reste dans le journal
 * d'audit en base, seul endroit autorisé à la conserver.
 */
public final class LogSafe {

    private LogSafe() {}

    /**
     * Masque un courriel : « jean.tremblay@ville.qc.ca » → « j***@ville.qc.ca ». Une valeur vide
     * devient « inconnu » ; un identifiant non-courriel (ex. « anonyme », « systeme ») est conservé
     * tel quel, car il ne désigne pas une personne.
     */
    public static String courriel(String valeur) {
        if (valeur == null || valeur.isBlank()) return "inconnu";
        int at = valeur.indexOf('@');
        if (at <= 0) return valeur;
        return valeur.charAt(0) + "***" + valeur.substring(at);
    }
}
