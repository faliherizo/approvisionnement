package qc.levis.contrat.clm.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/** Addenda 4 : niveau d'authentification par type de document (Q1) et signataire représentant une entreprise (Q6). */
class Addenda4DomaineTest {

    @Test
    void le_niveau_du_type_de_document_s_ajoute_a_la_politique_le_plus_exigeant_l_emporte() {
        assertEquals(Identite.Niveau.FORTE, Identite.auMoins(Identite.Niveau.COURRIEL, " forte "));
        assertEquals(Identite.Niveau.CODE_SMS, Identite.auMoins(Identite.Niveau.CODE_SMS, "COURRIEL"), "jamais d'abaissement");
        assertEquals(Identite.Niveau.CODE_SMS, Identite.auMoins(Identite.Niveau.CODE_SMS, "INCONNU"));
        assertEquals(Identite.Niveau.COURRIEL, Identite.auMoins(Identite.Niveau.COURRIEL, null));
        assertEquals(Identite.Niveau.COURRIEL, Identite.auMoins(Identite.Niveau.COURRIEL, " "));
    }

    private static Participant participant() {
        return new Participant(3L, 1L, "Marc Lavoie", "marc@agrinord.example", null, "SIGNATAIRE_1", true, Identite.Niveau.COURRIEL);
    }

    @Test
    void un_representant_porte_sa_qualite_et_son_entreprise_et_atteste_son_autorite_si_elle_est_exigee() {
        Participant p = participant();
        p.representer(" Président ", " Coopérative Agri-Nord inc. ", true);
        assertEquals("Président", p.getQualite());
        assertEquals("Coopérative Agri-Nord inc.", p.getEntreprise());
        assertTrue(p.isAutoriteRequise());
        assertEquals("Attestez être dûment autorisé à signer pour Coopérative Agri-Nord inc. avant de signer.",
                assertThrows(IllegalArgumentException.class, () -> p.exigerAutorite(false)).getMessage());
        assertDoesNotThrow(() -> p.exigerAutorite(true));

        Participant libre = participant();
        libre.representer(null, " ", false);
        assertNull(libre.getQualite());
        assertNull(libre.getEntreprise());
        assertDoesNotThrow(() -> libre.exigerAutorite(false), "aucune attestation exigée");
    }

    @Test
    void l_attestation_exigee_suppose_la_qualite_et_l_entreprise_et_les_longueurs_sont_bornees() {
        assertThrows(IllegalArgumentException.class, () -> participant().representer(null, "Agri-Nord", true));
        assertThrows(IllegalArgumentException.class, () -> participant().representer("Président", null, true));
        assertThrows(IllegalArgumentException.class, () -> participant().representer("x".repeat(151), "Agri-Nord", false));
        assertThrows(IllegalArgumentException.class, () -> participant().representer("Président", "x".repeat(201), false));
        assertDoesNotThrow(() -> participant().representer("x".repeat(150), "x".repeat(200), true));
        assertDoesNotThrow(() -> participant().representer(null, "x".repeat(200), false));
        assertDoesNotThrow(() -> participant().representer("Président", null, false));
    }
}
