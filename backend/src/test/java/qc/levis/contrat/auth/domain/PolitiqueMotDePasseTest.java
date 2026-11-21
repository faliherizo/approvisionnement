package qc.levis.contrat.auth.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/** Politique de mot de passe : longueur minimale et diversité des caractères ; message qui dit quoi corriger. */
class PolitiqueMotDePasseTest {

    @Test
    void un_mot_de_passe_conforme_est_accepte() {
        assertDoesNotThrow(() -> PolitiqueMotDePasse.exiger("Passw0rd!"));
        assertDoesNotThrow(() -> PolitiqueMotDePasse.exiger("bonjour2026!"), "minuscules, chiffres, spéciaux");
        assertDoesNotThrow(() -> PolitiqueMotDePasse.exiger("BonjourMonde7"), "minuscules, majuscules, chiffres");
    }

    @Test
    void trop_court_trop_uniforme_ou_absent_est_refuse_avec_la_consigne() {
        String message = assertThrows(IllegalArgumentException.class, () -> PolitiqueMotDePasse.exiger("abc")).getMessage();
        assertTrue(message.contains("au moins 8 caractères"), message);
        assertThrows(IllegalArgumentException.class, () -> PolitiqueMotDePasse.exiger("Ab1!"), "trop court");
        assertThrows(IllegalArgumentException.class, () -> PolitiqueMotDePasse.exiger("motdepasselong"), "une seule classe");
        assertThrows(IllegalArgumentException.class, () -> PolitiqueMotDePasse.exiger("motdepasse2026"), "deux classes");
        assertThrows(IllegalArgumentException.class, () -> PolitiqueMotDePasse.exiger(null));
    }

    @Test
    void les_classes_se_comptent_une_fois_chacune() {
        assertEquals(4, PolitiqueMotDePasse.classes("aA1!"));
        assertEquals(1, PolitiqueMotDePasse.classes("aaaa"));
        assertEquals(2, PolitiqueMotDePasse.classes("AAAA1111"));
    }
}
