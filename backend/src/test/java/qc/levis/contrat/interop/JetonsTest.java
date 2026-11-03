package qc.levis.contrat.interop;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import qc.levis.contrat.interop.application.Jetons;

/** Tests purs du hachage/vérification de jetons. */
class JetonsTest {

    @Test
    void hachage_stable_et_verifiable() {
        String secret = "mon-secret";
        String h = Jetons.hacher(secret);
        assertEquals(h, Jetons.hacher(secret));           // déterministe
        assertTrue(Jetons.verifier(secret, h));
        assertFalse(Jetons.verifier("autre", h));
        assertFalse(Jetons.verifier(null, h));
    }

    @Test
    void generation_aleatoire_distincte() {
        assertNotEquals(Jetons.generer(), Jetons.generer());
        assertEquals(64, Jetons.generer().length()); // 32 octets en hexadécimal
    }
}
