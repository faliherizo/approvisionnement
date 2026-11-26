package qc.levis.contrat.clm.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import org.junit.jupiter.api.Test;

/** Chiffrement au repos (Annexe 10, SEC-28 ; ADR-037) : enveloppe d'un document, clé de l'organisation, forme stockée. */
class ChiffrementDomaineTest {

    private static final Instant T0 = Instant.parse("2026-11-26T12:00:00Z");
    private static final byte[] IV = new byte[EnveloppeDocument.LONGUEUR_IV];

    @Test
    void l_enveloppe_porte_organisation_version_iv_et_chiffre_et_se_relit() {
        EnveloppeDocument e = new EnveloppeDocument(42L, 3, IV, new byte[]{9, 8, 7});
        byte[] octets = e.octets();
        assertTrue(EnveloppeDocument.estEnveloppe(octets));
        EnveloppeDocument lue = EnveloppeDocument.lire(octets);
        assertEquals(42L, lue.tenantId());
        assertEquals(3, lue.version());
        assertArrayEquals(IV, lue.iv());
        assertArrayEquals(new byte[]{9, 8, 7}, lue.chiffre());
        assertArrayEquals(EnveloppeDocument.entete(42L, 3), lue.entete());
        assertEquals(0, octets[0], "la marque commence par un octet nul");
    }

    @Test
    void un_contenu_en_clair_efface_ou_tronque_n_est_pas_une_enveloppe() {
        assertFalse(EnveloppeDocument.estEnveloppe(null));
        assertFalse(EnveloppeDocument.estEnveloppe(new byte[0]));
        assertFalse(EnveloppeDocument.estEnveloppe("%PDF-1.4 un ancien document en clair".getBytes()));
        byte[] tronque = new byte[EnveloppeDocument.LONGUEUR_ENTETE + EnveloppeDocument.LONGUEUR_IV];
        System.arraycopy(EnveloppeDocument.MARQUE, 0, tronque, 0, EnveloppeDocument.MARQUE.length);
        assertFalse(EnveloppeDocument.estEnveloppe(tronque), "en-tête et IV sans chiffré");
        assertThrows(IllegalArgumentException.class, () -> EnveloppeDocument.lire("%PDF".getBytes()));
    }

    @Test
    void une_enveloppe_invalide_est_refusee_et_ses_tableaux_sont_copies() {
        assertThrows(IllegalArgumentException.class, () -> new EnveloppeDocument(1L, 0, IV, new byte[]{1}));
        assertThrows(IllegalArgumentException.class, () -> new EnveloppeDocument(1L, 1, new byte[3], new byte[]{1}));
        assertThrows(IllegalArgumentException.class, () -> new EnveloppeDocument(1L, 1, IV, new byte[0]));
        byte[] chiffre = {1, 2};
        EnveloppeDocument e = new EnveloppeDocument(1L, 1, IV, chiffre);
        chiffre[0] = 99;
        e.chiffre()[1] = 99;
        e.iv()[0] = 5;
        assertArrayEquals(new byte[]{1, 2}, e.chiffre());
        assertArrayEquals(IV, e.iv());
    }

    @Test
    void la_cle_enveloppee_exige_un_identifiant_et_une_valeur_bornes() {
        assertThrows(IllegalArgumentException.class, () -> new CleEnveloppee(null, "v"));
        assertThrows(IllegalArgumentException.class, () -> new CleEnveloppee(" ", "v"));
        assertThrows(IllegalArgumentException.class, () -> new CleEnveloppee("x".repeat(301), "v"));
        assertThrows(IllegalArgumentException.class, () -> new CleEnveloppee("k", null));
        assertThrows(IllegalArgumentException.class, () -> new CleEnveloppee("k", " "));
        assertThrows(IllegalArgumentException.class, () -> new CleEnveloppee("k", "v".repeat(2049)));
        assertEquals("k", new CleEnveloppee("k", "v").cleMaitresse());
    }

    @Test
    void la_cle_de_l_organisation_se_retire_une_fois_et_se_reenveloppe() {
        assertThrows(IllegalArgumentException.class, () -> CleOrganisation.creer(7L, 0, new CleEnveloppee("k1", "v1"), T0));
        CleOrganisation c = CleOrganisation.creer(7L, 2, new CleEnveloppee("k1", "v1"), T0);
        assertTrue(c.estActive());
        assertEquals(CleOrganisation.Statut.ACTIVE, c.getStatut());
        assertEquals(7L, c.getTenantId());
        assertEquals(2, c.getVersion());
        assertEquals(T0, c.getCreeLe());
        assertNull(c.getId());
        assertNull(c.getRetireeLe());
        c.reenvelopper(new CleEnveloppee("k2", "v2"));
        assertEquals(new CleEnveloppee("k2", "v2"), c.enveloppe());
        assertEquals("k2", c.getCleMaitresse());
        c.retirer(T0.plusSeconds(60));
        assertFalse(c.estActive());
        assertEquals(T0.plusSeconds(60), c.getRetireeLe());
        assertThrows(IllegalStateException.class, () -> c.retirer(T0));
    }

    @Test
    void le_document_garde_le_clair_en_memoire_et_l_enveloppe_en_stockage() {
        byte[] clair = "%PDF-clair".getBytes();
        DocumentTrousse d = new DocumentTrousse(7L, 1L, "c", 1, "a.pdf", FormatDocument.Type.PDF, clair, "i", null, T0);
        assertEquals(7L, d.getTenantId());
        assertNull(d.getCleVersion());
        assertSame(clair, d.getStockage(), "avant l'insertion, la forme stockée est le clair");
        byte[] enveloppe = {0, 1, 2};
        d.stockerChiffre(enveloppe, 4);
        assertSame(enveloppe, d.getStockage());
        assertSame(clair, d.getContenu(), "le clair reste lisible dans la transaction");
        assertEquals(4, d.getCleVersion());
        byte[] autre = {0, 3};
        d.stockerChiffre(autre, 5);
        assertSame(clair, d.getContenu(), "un réenveloppement garde le clair déjà connu");
        assertEquals(clair.length, d.getTaille(), "taille et empreinte restent celles du clair");
    }

    @Test
    void au_chargement_le_clair_dechiffre_remplace_la_lecture_de_l_enveloppe() {
        DocumentTrousse d = new DocumentTrousse(7L, 1L, "c", 1, "a.pdf", FormatDocument.Type.PDF, "%PDF".getBytes(), "i", null, T0);
        d.dechiffre("lu".getBytes());
        assertArrayEquals("lu".getBytes(), d.getContenu());
        d.effacerContenu();
        assertEquals(0, d.getContenu().length, "la destruction efface aussi le clair en mémoire");
        assertEquals(0, d.getStockage().length);
    }
}
