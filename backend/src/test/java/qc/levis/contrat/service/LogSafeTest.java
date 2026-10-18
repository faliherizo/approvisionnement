package qc.levis.contrat.service;

import org.junit.jupiter.api.Test;
import qc.levis.contrat.commun.application.LogSafe;
import static org.junit.jupiter.api.Assertions.*;

/** Tests du masquage des identifiants personnels pour les journaux techniques. */
class LogSafeTest {

    @Test
    void courriel_masqueLaPartieLocale() {
        assertEquals("j***@ville.qc.ca", LogSafe.courriel("jean.tremblay@ville.qc.ca"));
    }

    @Test
    void courriel_videOuNull_donneInconnu() {
        assertEquals("inconnu", LogSafe.courriel(null));
        assertEquals("inconnu", LogSafe.courriel("   "));
    }

    @Test
    void courriel_identifiantNonPersonnel_conserve() {
        assertEquals("anonyme", LogSafe.courriel("anonyme"));   // pas une personne → conservé
    }
}
