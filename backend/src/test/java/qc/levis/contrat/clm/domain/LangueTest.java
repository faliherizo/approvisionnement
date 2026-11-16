package qc.levis.contrat.clm.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/** Langue de communication d'un participant (addenda 2, Q19) : français par défaut, anglais sur demande. */
class LangueTest {

    @Test
    void une_saisie_vide_donne_le_francais_et_la_casse_est_indifferente() {
        assertEquals(Langue.FR, Langue.de(null));
        assertEquals(Langue.FR, Langue.de("  "));
        assertEquals(Langue.EN, Langue.de(" en "));
        assertEquals("en", Langue.EN.code());
        assertEquals("fr", Langue.FR.code());
    }

    @Test
    void une_autre_langue_est_refusee() {
        assertEquals("Langue de communication : FR ou EN.", assertThrows(IllegalArgumentException.class, () -> Langue.de("es")).getMessage());
    }

    @Test
    void le_participant_communique_en_francais_tant_qu_aucune_autre_langue_n_est_choisie() {
        Participant p = new Participant(3L, 1L, "Alice", "alice@exemple.ca", null, "SIGNATAIRE_1", true, Identite.Niveau.COURRIEL);
        assertEquals(Langue.FR, p.getLangue());
        p.choisirLangue(Langue.EN);
        assertEquals(Langue.EN, p.getLangue());
        p.choisirLangue(null);
        assertEquals(Langue.FR, p.getLangue());
    }
}
