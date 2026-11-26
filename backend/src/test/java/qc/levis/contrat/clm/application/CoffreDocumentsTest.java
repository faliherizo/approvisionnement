package qc.levis.contrat.clm.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import qc.levis.contrat.clm.application.port.AleaCryptographique;
import qc.levis.contrat.clm.application.port.GestionnaireCles;
import qc.levis.contrat.clm.domain.CleEnveloppee;
import qc.levis.contrat.clm.domain.CleOrganisation;
import qc.levis.contrat.clm.domain.EnveloppeDocument;

/** Chiffrement d'enveloppe par organisation (ADR-037) : clair rétabli, organisations étanches, clés en cache, rotation relue. */
class CoffreDocumentsTest {

    private static final Instant T0 = Instant.parse("2026-11-26T12:00:00Z");
    private static final byte[] CLAIR = "%PDF-1.4 contrat confidentiel".getBytes();

    private final RegistreClesOrganisation registre = mock(RegistreClesOrganisation.class);
    /** KMS factice : la clé de données « enveloppée » est sa propre valeur ; une clé distincte par organisation. */
    private final GestionnaireCles kms = mock(GestionnaireCles.class);
    private final AleaCryptographique alea = n -> {
        byte[] o = new byte[n];
        new SecureRandom().nextBytes(o);
        return o;
    };
    private Instant maintenant = T0;
    private CoffreDocuments coffre;

    @BeforeEach
    void preparer() {
        Clock horloge = new Clock() {
            @Override public ZoneOffset getZone() { return ZoneOffset.UTC; }
            @Override public Clock withZone(java.time.ZoneId z) { return this; }
            @Override public Instant instant() { return maintenant; }
        };
        coffre = new CoffreDocuments(registre, kms, alea, horloge);
        cle(1L, 1, (byte) 1);
        cle(2L, 1, (byte) 2);
        when(registre.active(1L)).thenReturn(CleOrganisation.creer(1L, 1, new CleEnveloppee("k", "01"), T0));
        when(registre.active(2L)).thenReturn(CleOrganisation.creer(2L, 1, new CleEnveloppee("k", "02"), T0));
        when(kms.desenvelopper(any())).thenAnswer(i -> {
            byte[] cle = new byte[32];
            java.util.Arrays.fill(cle, Byte.parseByte(((CleEnveloppee) i.getArgument(0)).valeur()));
            return cle;
        });
    }

    private void cle(Long tenant, int version, byte octet) {
        when(registre.version(tenant, version)).thenReturn(Optional.of(
                CleOrganisation.creer(tenant, version, new CleEnveloppee("k", String.format("%02d", octet)), T0)));
    }

    @Test
    void le_clair_se_retrouve_et_l_enveloppe_ne_le_contient_pas() {
        CoffreDocuments.Chiffre c = coffre.chiffrer(1L, CLAIR);
        assertEquals(1, c.version());
        assertTrue(EnveloppeDocument.estEnveloppe(c.octets()));
        assertFalse(new String(c.octets()).contains("contrat confidentiel"));
        assertArrayEquals(CLAIR, coffre.dechiffrer(1L, c.octets()));
        assertNotEquals(java.util.Arrays.toString(c.octets()), java.util.Arrays.toString(coffre.chiffrer(1L, CLAIR).octets()), "IV aléatoire");
    }

    @Test
    void la_cle_d_une_autre_organisation_n_ouvre_pas_le_document() {
        byte[] doc = coffre.chiffrer(1L, CLAIR).octets();
        assertThrows(CoffreDocuments.DocumentIllisible.class, () -> coffre.dechiffrer(2L, doc), "en-tête d'une autre organisation");
        byte[] maquille = doc.clone();
        java.nio.ByteBuffer.wrap(maquille).putLong(5, 2L);
        assertThrows(CoffreDocuments.DocumentIllisible.class, () -> coffre.dechiffrer(2L, maquille), "clé de l'organisation 2 sur le chiffré de la 1");
        byte[] altere = doc.clone();
        altere[altere.length - 1] ^= 1;
        assertThrows(CoffreDocuments.DocumentIllisible.class, () -> coffre.dechiffrer(1L, altere), "étiquette GCM");
    }

    @Test
    void un_ancien_contenu_en_clair_ou_efface_se_lit_tel_quel() {
        assertArrayEquals(CLAIR, coffre.dechiffrer(1L, CLAIR));
        CoffreDocuments.Chiffre vide = coffre.chiffrer(1L, new byte[0]);
        assertEquals(0, vide.octets().length);
        assertEquals(1, vide.version());
    }

    @Test
    void les_cles_ouvertes_restent_en_memoire_et_la_version_active_se_relit_apres_fraicheur_ou_oubli() {
        byte[] doc = coffre.chiffrer(1L, CLAIR).octets();
        coffre.dechiffrer(1L, doc);
        coffre.chiffrer(1L, CLAIR);
        verify(registre, times(1)).version(1L, 1);
        verify(registre, times(1)).active(1L);
        maintenant = T0.plus(CoffreDocuments.FRAICHEUR_ACTIVE).plus(Duration.ofSeconds(1));
        coffre.chiffrer(1L, CLAIR);
        verify(registre, times(2)).active(1L);
        coffre.oublier(1L);
        coffre.dechiffrer(1L, doc);
        verify(registre, times(2)).version(1L, 1);
    }

    @Test
    void une_creation_concurrente_de_la_premiere_cle_relit_celle_de_l_autre() {
        when(registre.active(3L)).thenThrow(new DataIntegrityViolationException("uk")).thenReturn(
                CleOrganisation.creer(3L, 1, new CleEnveloppee("k", "03"), T0));
        cle(3L, 1, (byte) 3);
        assertEquals(1, coffre.chiffrer(3L, CLAIR).version());
    }

    @Test
    void une_version_de_cle_introuvable_rend_le_document_illisible() {
        byte[] doc = new EnveloppeDocument(1L, 9, new byte[12], new byte[20]).octets();
        when(registre.version(1L, 9)).thenReturn(Optional.empty());
        assertThrows(CoffreDocuments.DocumentIllisible.class, () -> coffre.dechiffrer(1L, doc));
    }
}
