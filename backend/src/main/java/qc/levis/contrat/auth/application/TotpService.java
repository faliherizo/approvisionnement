package qc.levis.contrat.auth.application;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Service;

/**
 * TOTP (Time-based One-Time Password, RFC 6238) — HMAC-SHA1, pas de 30 s, 6 chiffres.
 * Compatible avec Google Authenticator, Microsoft Authenticator, etc.
 */
@Service
public class TotpService {

    private static final String BASE32 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final int PAS_SECONDES = 30;
    private static final int CHIFFRES = 6;
    private static final int FENETRE = 1; // tolère ±1 pas (dérive d'horloge)
    private final SecureRandom random = new SecureRandom();

    /** Génère un secret base32 aléatoire (160 bits). */
    public String genererSecret() {
        byte[] buf = new byte[20];
        random.nextBytes(buf);
        StringBuilder sb = new StringBuilder();
        int bits = 0, value = 0;
        for (byte b : buf) {
            value = (value << 8) | (b & 0xFF);
            bits += 8;
            while (bits >= 5) {
                sb.append(BASE32.charAt((value >>> (bits - 5)) & 31));
                bits -= 5;
            }
        }
        return sb.toString();
    }

    /** URI otpauth:// à encoder en QR code pour l'application d'authentification. */
    public String uriOtpauth(String secret, String compte, String emetteur) {
        return "otpauth://totp/" + emetteur + ":" + compte
                + "?secret=" + secret + "&issuer=" + emetteur + "&digits=" + CHIFFRES
                + "&period=" + PAS_SECONDES;
    }

    /** Vérifie un code par rapport au temps courant, avec fenêtre de tolérance. */
    public boolean verifier(String secret, String code) {
        if (secret == null || code == null) return false;
        String saisi = code.trim();
        long pas = System.currentTimeMillis() / 1000L / PAS_SECONDES;
        for (int i = -FENETRE; i <= FENETRE; i++) {
            if (genererCode(secret, pas + i).equals(saisi)) {
                return true;
            }
        }
        return false;
    }

    private String genererCode(String secret, long compteur) {
        byte[] cle = decoderBase32(secret);
        byte[] data = ByteBuffer.allocate(8).putLong(compteur).array();
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(cle, "HmacSHA1"));
            byte[] hash = mac.doFinal(data);
            int offset = hash[hash.length - 1] & 0xF;
            int binaire = ((hash[offset] & 0x7F) << 24)
                    | ((hash[offset + 1] & 0xFF) << 16)
                    | ((hash[offset + 2] & 0xFF) << 8)
                    | (hash[offset + 3] & 0xFF);
            int otp = binaire % (int) Math.pow(10, CHIFFRES);
            return String.format("%0" + CHIFFRES + "d", otp);
        } catch (Exception e) {
            throw new IllegalStateException("Erreur de calcul TOTP", e);
        }
    }

    private byte[] decoderBase32(String s) {
        String clean = s.replace("=", "").toUpperCase();
        ByteBuffer buffer = ByteBuffer.allocate(clean.length() * 5 / 8);
        int bits = 0, value = 0;
        for (char c : clean.toCharArray()) {
            int idx = BASE32.indexOf(c);
            if (idx < 0) continue;
            value = (value << 5) | idx;
            bits += 5;
            if (bits >= 8) {
                buffer.put((byte) ((value >>> (bits - 8)) & 0xFF));
                bits -= 8;
            }
        }
        byte[] out = new byte[buffer.position()];
        buffer.rewind();
        buffer.get(out);
        return out;
    }
}
