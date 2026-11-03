package qc.levis.contrat.interop.application;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;

/** Utilitaires PURS de jetons/secrets d'API : hachage SHA-256 et génération aléatoire. Testable. */
public final class Jetons {

    private static final SecureRandom RNG = new SecureRandom();

    private Jetons() {}

    public static String hacher(String valeur) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(valeur.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 indisponible", e);
        }
    }

    public static boolean verifier(String valeur, String hash) {
        return valeur != null && hash != null && MessageDigest.isEqual(
                hacher(valeur).getBytes(StandardCharsets.UTF_8), hash.getBytes(StandardCharsets.UTF_8));
    }

    public static String generer() {
        byte[] b = new byte[32];
        RNG.nextBytes(b);
        return HexFormat.of().formatHex(b);
    }
}
