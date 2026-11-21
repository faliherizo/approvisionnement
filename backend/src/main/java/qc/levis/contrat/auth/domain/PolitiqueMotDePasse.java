package qc.levis.contrat.auth.domain;

/**
 * Politique de mot de passe des comptes locaux (exigence des organisations : 2FA et politique de mots de passe ;
 * les comptes d'annuaire, SSO ou SCIM, n'ont pas de mot de passe local). Au moins 8 caractères et au moins trois des
 * quatre classes : minuscule, majuscule, chiffre, caractère spécial.
 */
public final class PolitiqueMotDePasse {

    public static final int LONGUEUR_MIN = 8;
    public static final int CLASSES_MIN = 3;

    private PolitiqueMotDePasse() {
    }

    /** Refuse un mot de passe hors politique, avec un message qui dit quoi corriger. */
    public static void exiger(String motDePasse) {
        if (motDePasse == null || motDePasse.length() < LONGUEUR_MIN || classes(motDePasse) < CLASSES_MIN) {
            throw new IllegalArgumentException("Mot de passe trop faible : au moins " + LONGUEUR_MIN
                    + " caractères et trois types parmi minuscules, majuscules, chiffres et caractères spéciaux.");
        }
    }

    static int classes(String s) {
        int n = 0;
        if (s.chars().anyMatch(Character::isLowerCase)) n++;
        if (s.chars().anyMatch(Character::isUpperCase)) n++;
        if (s.chars().anyMatch(Character::isDigit)) n++;
        if (s.chars().anyMatch(c -> !Character.isLetterOrDigit(c))) n++;
        return n;
    }
}
