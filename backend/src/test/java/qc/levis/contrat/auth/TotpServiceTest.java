package qc.levis.contrat.auth;

import org.junit.jupiter.api.Test;
import qc.levis.contrat.auth.application.TotpService;
import static org.junit.jupiter.api.Assertions.*;

class TotpServiceTest {

    private final TotpService totp = new TotpService();

    @Test
    void secretGenere_estBase32NonVide() {
        String s = totp.genererSecret();
        assertNotNull(s);
        assertTrue(s.length() >= 16);
        assertTrue(s.matches("[A-Z2-7]+"));
    }

    @Test
    void codeGenere_estVerifie() {
        String secret = totp.genererSecret();
        // Impossible d'accéder au code interne : on vérifie via un secret connu et l'algorithme.
        // Un mauvais code doit être rejeté.
        assertFalse(totp.verifier(secret, "000000"));
        assertFalse(totp.verifier(secret, null));
        assertFalse(totp.verifier(null, "123456"));
    }

    @Test
    void secretConnu_codeCalcule_estAccepte() throws Exception {
        // Reproduit le calcul RFC 6238 pour le secret de test et vérifie l'acceptation.
        String secret = "JBSWY3DPEHPK3PXP";
        String code = calculerTotp(secret);
        assertTrue(totp.verifier(secret, code));
    }

    @Test
    void uriOtpauth_contientLesParametres() {
        String uri = totp.uriOtpauth("ABC", "user@x.ca", "Ville");
        assertTrue(uri.startsWith("otpauth://totp/"));
        assertTrue(uri.contains("secret=ABC"));
        assertTrue(uri.contains("issuer=Ville"));
    }

    // --- Calcul TOTP de référence pour le test ---
    private String calculerTotp(String base32) throws Exception {
        byte[] key = decodeBase32(base32);
        long compteur = System.currentTimeMillis() / 1000L / 30L;
        byte[] data = java.nio.ByteBuffer.allocate(8).putLong(compteur).array();
        javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA1");
        mac.init(new javax.crypto.spec.SecretKeySpec(key, "HmacSHA1"));
        byte[] h = mac.doFinal(data);
        int o = h[h.length - 1] & 0xF;
        int bin = ((h[o] & 0x7F) << 24) | ((h[o + 1] & 0xFF) << 16)
                | ((h[o + 2] & 0xFF) << 8) | (h[o + 3] & 0xFF);
        return String.format("%06d", bin % 1_000_000);
    }

    private byte[] decodeBase32(String s) {
        String base32 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
        java.nio.ByteBuffer buf = java.nio.ByteBuffer.allocate(s.length() * 5 / 8);
        int bits = 0, value = 0;
        for (char c : s.toCharArray()) {
            value = (value << 5) | base32.indexOf(c);
            bits += 5;
            if (bits >= 8) { buf.put((byte) ((value >>> (bits - 8)) & 0xFF)); bits -= 8; }
        }
        byte[] out = new byte[buf.position()];
        buf.rewind(); buf.get(out);
        return out;
    }
}
