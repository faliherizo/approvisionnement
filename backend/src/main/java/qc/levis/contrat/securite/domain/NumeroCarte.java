package qc.levis.contrat.securite.domain;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Détection d'un numéro de carte de paiement (PAN) dans un texte libre (IQ-D-14, ADR-022). Un PAN est refusé à la
 * frontière pour que la plateforme reste hors du périmètre PCI DSS. Trois conditions cumulées limitent les faux
 * positifs : préfixe d'un réseau émetteur, longueur propre à ce réseau, clé de Luhn.
 */
public final class NumeroCarte {

    /** Réseau émetteur reconnu ; le seul renseignement que la journalisation retient d'un refus. */
    public enum Reseau { VISA, MASTERCARD, AMEX, DISCOVER, JCB, DINERS, UNIONPAY }

    /** 13 à 19 chiffres, séparés au plus par un espace ou un tiret isolé, non collés à d'autres chiffres. */
    private static final Pattern CANDIDAT = Pattern.compile("(?<![\\d-])\\d(?:[ -]?\\d){12,18}(?![\\d])");

    private NumeroCarte() {
    }

    /** Réseau du premier numéro de carte trouvé dans le texte, s'il y en a un. */
    public static Optional<Reseau> detecter(CharSequence texte) {
        if (texte == null || texte.length() < 13) {
            return Optional.empty();
        }
        Matcher m = CANDIDAT.matcher(texte);
        while (m.find()) {
            Optional<Reseau> r = reseau(m.group().replaceAll("[ -]", ""));
            if (r.isPresent()) {
                return r;
            }
        }
        return Optional.empty();
    }

    /** Réseau d'une suite de chiffres qui a le préfixe, la longueur et la clé de Luhn d'une carte ; sinon vide. */
    static Optional<Reseau> reseau(String chiffres) {
        Reseau r = parPrefixe(chiffres);
        return r != null && longueurValide(r, chiffres.length()) && luhn(chiffres) ? Optional.of(r) : Optional.empty();
    }

    private static Reseau parPrefixe(String n) {
        int p2 = Integer.parseInt(n.substring(0, 2));
        int p3 = Integer.parseInt(n.substring(0, 3));
        int p4 = Integer.parseInt(n.substring(0, 4));
        if (n.charAt(0) == '4') return Reseau.VISA;
        if (p2 >= 51 && p2 <= 55 || p4 >= 2221 && p4 <= 2720) return Reseau.MASTERCARD;
        if (p2 == 34 || p2 == 37) return Reseau.AMEX;
        if (p4 == 6011 || p2 == 65 || p3 >= 644 && p3 <= 649) return Reseau.DISCOVER;
        if (p4 >= 3528 && p4 <= 3589) return Reseau.JCB;
        if (p3 >= 300 && p3 <= 305 || p2 == 36 || p2 == 38) return Reseau.DINERS;
        if (p2 == 62) return Reseau.UNIONPAY;
        return null;
    }

    private static boolean longueurValide(Reseau r, int longueur) {
        switch (r) {
            case VISA: return longueur == 13 || longueur == 16 || longueur == 19;
            case AMEX: return longueur == 15;
            case DINERS: return longueur >= 14 && longueur <= 19;
            case MASTERCARD: return longueur == 16;
            default: return longueur >= 16 && longueur <= 19;
        }
    }

    /** Clé de Luhn (ISO/IEC 7812-1). */
    static boolean luhn(String chiffres) {
        int somme = 0;
        boolean doubler = false;
        for (int i = chiffres.length() - 1; i >= 0; i--) {
            int d = chiffres.charAt(i) - '0';
            if (doubler) {
                d = d * 2 > 9 ? d * 2 - 9 : d * 2;
            }
            somme += d;
            doubler = !doubler;
        }
        return somme % 10 == 0;
    }
}
