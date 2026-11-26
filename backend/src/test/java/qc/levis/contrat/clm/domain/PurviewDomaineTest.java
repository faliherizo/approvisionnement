package qc.levis.contrat.clm.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;

/** Étiquettes Microsoft Purview (Annexe 10, SEC-27 ; ADR-036) : étiquette, politique, document étiqueté, dépôt. */
class PurviewDomaineTest {

    private static final Instant T0 = Instant.parse("2026-11-26T12:00:00Z");
    private static final ZoneId QC = ZoneId.of("America/Toronto");
    private static final EtiquettePurview CONFIDENTIEL = EtiquettePurview.sensibilite("id-c", "Confidentiel");
    private static final EtiquettePurview SEPT_ANS = EtiquettePurview.retention("Contrats - 7 ans", 2557);

    @Test
    void une_etiquette_exige_type_nom_et_identifiant_bornes() {
        assertThrows(IllegalArgumentException.class, () -> new EtiquettePurview(null, "i", "n", null));
        assertThrows(IllegalArgumentException.class, () -> EtiquettePurview.sensibilite("i", null));
        assertThrows(IllegalArgumentException.class, () -> EtiquettePurview.sensibilite("i", " "));
        assertThrows(IllegalArgumentException.class, () -> EtiquettePurview.sensibilite("i", "n".repeat(101)));
        assertThrows(IllegalArgumentException.class, () -> EtiquettePurview.sensibilite(null, "n"));
        assertThrows(IllegalArgumentException.class, () -> EtiquettePurview.sensibilite(" ", "n"));
        assertThrows(IllegalArgumentException.class, () -> EtiquettePurview.sensibilite("i".repeat(65), "n"));
        assertThrows(IllegalArgumentException.class, () -> new EtiquettePurview(EtiquettePurview.Type.SENSIBILITE, "i", "n", 3));
        assertThrows(IllegalArgumentException.class, () -> EtiquettePurview.retention("r", -1));
        assertEquals("Contrats - 7 ans", SEPT_ANS.id(), "une rétention se désigne par son nom");
        assertEquals(EtiquettePurview.Type.RETENTION, SEPT_ANS.type());
    }

    @Test
    void la_fin_de_retention_court_depuis_la_cloture() {
        assertEquals(LocalDate.of(2033, 11, 26), SEPT_ANS.finRetention(T0, QC));
        assertNull(SEPT_ANS.finRetention(null, QC), "trousse non close");
        assertNull(EtiquettePurview.retention("Permanente", null).finRetention(T0, QC), "rétention sans durée");
    }

    @Test
    void la_politique_porte_au_moins_une_etiquette_chacune_de_son_type() {
        assertThrows(IllegalArgumentException.class, () -> new PolitiquePurview(1L, " "));
        assertThrows(IllegalArgumentException.class, () -> new PolitiquePurview(1L, "x".repeat(81)));
        PolitiquePurview p = new PolitiquePurview(1L, "SUBVENTION");
        assertThrows(IllegalArgumentException.class, () -> p.definir(null, null, "a", T0));
        assertThrows(IllegalArgumentException.class, () -> p.definir(SEPT_ANS, null, "a", T0));
        assertThrows(IllegalArgumentException.class, () -> p.definir(null, CONFIDENTIEL, "a", T0));
        p.definir(CONFIDENTIEL, SEPT_ANS, "admin", T0);
        assertEquals(CONFIDENTIEL, p.sensibilite());
        assertEquals(SEPT_ANS, p.retention());
        assertFalse(p.estDefaut());
        assertEquals("SUBVENTION", p.getTypeContrat());
        assertEquals(1L, p.getTenantId());
        assertEquals("admin", p.getModifiePar());
        assertEquals(T0, p.getModifieLe());
        assertNull(p.getId());
        p.definir(null, SEPT_ANS, "admin", T0);
        assertNull(p.sensibilite());
        p.definir(CONFIDENTIEL, null, "admin", T0);
        assertNull(p.retention());
        assertTrue(new PolitiquePurview(1L, null).estDefaut());
    }

    @Test
    void le_document_garde_ses_etiquettes_et_sa_version_signee_les_reprend() {
        DocumentTrousse source = document();
        assertNull(source.sensibilite());
        assertNull(source.retention());
        source.etiqueter(CONFIDENTIEL, SEPT_ANS);
        assertEquals(CONFIDENTIEL, source.sensibilite());
        assertEquals(SEPT_ANS, source.retention());
        DocumentTrousse signee = document();
        signee.reprendreEtiquettes(source);
        assertEquals(CONFIDENTIEL, signee.sensibilite());
        assertEquals(SEPT_ANS, signee.retention());
        signee.etiqueter(null, null);
        assertNull(signee.sensibilite());
        assertNull(signee.retention());
    }

    @Test
    void le_depot_note_le_resultat_de_la_propagation_tronque() {
        DepotM365 d = new DepotM365(1L, 2L, DepotM365.Nature.DOCUMENT_SIGNE, 3L, T0);
        assertNull(d.getEtiquette());
        d.noterEtiquette("Confidentiel — apposées");
        assertEquals("Confidentiel — apposées", d.getEtiquette());
        d.noterEtiquette("x".repeat(400));
        assertEquals(DepotM365.ETIQUETTE_MAX, d.getEtiquette().length());
    }

    private static DocumentTrousse document() {
        return new DocumentTrousse(1L, 2L, "c", 1, "a.pdf", FormatDocument.Type.PDF, "%PDF".getBytes(), "i", null, T0);
    }
}
