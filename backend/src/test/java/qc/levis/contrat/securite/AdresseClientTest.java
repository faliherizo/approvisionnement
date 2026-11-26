package qc.levis.contrat.securite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import qc.levis.contrat.securite.domain.AdresseClient;

/** SEC-33 : l'adresse imputée au client ne se forge pas par X-Forwarded-For. */
class AdresseClientTest {

    @Test
    void une_requete_recue_d_une_adresse_publique_ignore_l_en_tete() {
        assertEquals("203.0.113.7", AdresseClient.resoudre("203.0.113.7", "198.51.100.1"));
    }

    @Test
    void derriere_le_frontal_on_retient_l_adresse_ajoutee_par_le_mandataire_et_non_celle_forgee_par_le_client() {
        // Le client forge « 1.2.3.4 » ; nginx ajoute l'adresse réelle (203.0.113.7) en fin de chaîne.
        assertEquals("203.0.113.7", AdresseClient.resoudre("172.18.0.3", "1.2.3.4, 203.0.113.7"));
    }

    @Test
    void plusieurs_mandataires_prives_sont_traverses_de_droite_a_gauche() {
        assertEquals("203.0.113.7", AdresseClient.resoudre("10.0.0.2", "9.9.9.9, 203.0.113.7, 192.168.1.10, 10.0.0.5"));
    }

    @Test
    void une_entree_qui_n_est_pas_une_adresse_arrete_la_lecture_sans_resolution_dns() {
        assertEquals("192.168.1.10", AdresseClient.resoudre("10.0.0.2", "cafe.bad, 192.168.1.10"));
        assertEquals("10.0.0.2", AdresseClient.resoudre("10.0.0.2", "999.1.1.1"));
    }

    @Test
    void sans_en_tete_ou_avec_une_chaine_entierement_privee_on_garde_l_adresse_connue_la_plus_lointaine() {
        assertEquals("127.0.0.1", AdresseClient.resoudre("127.0.0.1", null));
        assertEquals("127.0.0.1", AdresseClient.resoudre("127.0.0.1", "  "));
        assertEquals("10.1.1.1", AdresseClient.resoudre("127.0.0.1", "10.1.1.1, 192.168.0.4"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"127.0.0.1", "10.2.3.4", "172.16.0.1", "192.168.0.1", "169.254.1.1", "::1", "fd00::1", "fe80::1"})
    void les_adresses_de_l_hebergement_sont_des_mandataires(String a) {
        assertTrue(estMandataire(a), a);
    }

    @ParameterizedTest
    @ValueSource(strings = {"203.0.113.7", "2001:db8::1", "252.1.1.1", "nom.example", "1:2:3:zz", "1:2:3:4:5:6:7:8:9", "256.0.0.1"})
    void les_autres_adresses_ou_valeurs_ne_le_sont_pas(String a) {
        assertFalse(estMandataire(a), a);
    }

    @ParameterizedTest
    @CsvSource({"2001:db8::1, 2001:db8::1", "::1, ::1"})
    void ipv6_est_prise_en_charge(String distante, String attendue) {
        assertEquals(attendue, AdresseClient.resoudre(distante, null));
    }

    @Test
    void une_adresse_distante_absente_n_est_pas_un_mandataire() {
        assertFalse(estMandataire(null));
        assertEquals(null, AdresseClient.resoudre(null, "203.0.113.7"));
    }

    private static boolean estMandataire(String a) {
        // Un mandataire est reconnu à ce qu'on lit l'en-tête derrière lui.
        return "203.0.113.99".equals(AdresseClient.resoudre(a, "203.0.113.99"));
    }
}
