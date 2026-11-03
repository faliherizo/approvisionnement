package qc.levis.contrat.clm.domain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

/**
 * Journal scellé d'une trousse (ST-10/ST-14) : chaque événement porte l'empreinte du précédent, de sorte qu'une
 * modification, une suppression ou une insertion a posteriori rompt la chaîne et se détecte à la vérification.
 */
public final class ChaineScellee {

    public static final String ORIGINE = "0".repeat(64);

    public record Maillon(long sequence, String type, String acteur, Instant horodatage, String detail,
                          String empreintePrecedente, String empreinte) {
    }

    /** Résultat d'une vérification : {@code rupture} désigne la première séquence fautive. */
    public record Verification(boolean integre, Optional<Long> rupture) {
    }

    private ChaineScellee() {
    }

    public static String empreinte(String precedente, long trousseId, long sequence, String type, String acteur,
                                   Instant horodatage, String detail) {
        String canon = String.join("|", precedente, Long.toString(trousseId), Long.toString(sequence),
                nul(type), nul(acteur), horodatage.toString(), nul(detail));
        return sha256(canon.getBytes(StandardCharsets.UTF_8));
    }

    public static Verification verifier(long trousseId, List<Maillon> maillons) {
        String precedente = ORIGINE;
        long attendue = 1;
        for (Maillon m : maillons) {
            String recalculee = empreinte(precedente, trousseId, m.sequence(), m.type(), m.acteur(), m.horodatage(), m.detail());
            if (m.sequence() != attendue || !precedente.equals(m.empreintePrecedente()) || !recalculee.equals(m.empreinte())) {
                return new Verification(false, Optional.of(m.sequence()));
            }
            precedente = m.empreinte();
            attendue++;
        }
        return new Verification(true, Optional.empty());
    }

    public static String sha256(byte[] octets) {
        return condenser("SHA-256", octets);
    }

    /** L'indisponibilité d'un algorithme devient une erreur typée plutôt qu'une exception contrôlée propagée. */
    static String condenser(String algorithme, byte[] octets) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance(algorithme).digest(octets));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(algorithme + " indisponible", e);
        }
    }

    private static String nul(String s) {
        return s == null ? "" : s.replace("|", "¦");
    }
}
