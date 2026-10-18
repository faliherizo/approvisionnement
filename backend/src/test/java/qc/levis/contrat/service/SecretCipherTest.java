package qc.levis.contrat.service;

import org.junit.jupiter.api.Test;
import qc.levis.contrat.commun.application.SecretCipher;
import static org.junit.jupiter.api.Assertions.*;

/** Tests unitaires du chiffrement des secrets (aller-retour, valeurs vides). */
class SecretCipherTest {

    private final SecretCipher cipher = new SecretCipher("cle-de-test-32-octets-minimum!!", "5c0ff33a1b2c3d4e");

    @Test
    void chiffrer_puis_dechiffrer_restitueLeClair() {
        String secret = "S3cr3t-Client-M365";
        String chiffre = cipher.chiffrer(secret);
        assertNotEquals(secret, chiffre);            // stocké chiffré, jamais en clair
        assertEquals(secret, cipher.dechiffrer(chiffre));
    }

    @Test
    void chiffrer_valeurVideOuNulle_renvoieNull() {
        assertNull(cipher.chiffrer(null));
        assertNull(cipher.chiffrer("   "));
    }

    @Test
    void dechiffrer_valeurVideOuNulle_renvoieNull() {
        assertNull(cipher.dechiffrer(null));
        assertNull(cipher.dechiffrer(""));
    }
}
