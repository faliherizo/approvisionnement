package qc.levis.contrat.vitrine.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Contenu d'une vitrine : bornes, fonctionnalités relues telles que saisies, chemins de média sans remontée. */
class VitrineTenantTest {

    private static final Instant LE = Instant.parse("2026-11-25T10:00:00Z");

    private static VitrineTenant.Contenu contenu(List<VitrineTenant.Fonctionnalite> f, String video) {
        return new VitrineTenant.Contenu(" IQ ", "  Accroche  ", f, video, "vitrines/iq/presentation.vtt", " ", true);
    }

    @Test
    void le_contenu_est_normalise_et_les_fonctionnalites_relues_telles_quelles() {
        VitrineTenant v = VitrineTenant.nouvelle(3L, "Investissement Québec");
        assertEquals("Investissement Québec", v.getTitre());
        assertFalse(v.isPubliee());
        assertEquals(List.of(), v.getFonctionnalites());

        v.modifier(contenu(List.of(new VitrineTenant.Fonctionnalite("Signature", "En ligne :: et sur téléphone"),
                new VitrineTenant.Fonctionnalite("Lot", null)), "vitrines/iq/presentation.mp4"), "sa", LE);

        assertEquals("IQ", v.getTitre());
        assertEquals("Accroche", v.getAccroche());
        assertEquals(List.of(new VitrineTenant.Fonctionnalite("Signature", "En ligne : et sur téléphone"),
                new VitrineTenant.Fonctionnalite("Lot", "")), v.getFonctionnalites(), "le séparateur saisi ne casse pas la relecture");
        assertEquals("vitrines/iq/presentation.mp4", v.getVideo());
        assertEquals("vitrines/iq/presentation.vtt", v.getSousTitres());
        assertNull(v.getAffiche(), "vide = aucune affiche");
        assertTrue(v.isPubliee());
        assertEquals("sa", v.getMajPar());
        assertEquals(LE, v.getMajLe());
        assertEquals(3L, v.getTenantId());
        assertNull(v.getId());
    }

    @Test
    void les_bornes_et_les_champs_obligatoires_sont_controles() {
        VitrineTenant v = VitrineTenant.nouvelle(3L, "IQ");
        assertThrows(IllegalArgumentException.class, () -> VitrineTenant.nouvelle(3L, " "));
        assertThrows(IllegalArgumentException.class, () -> v.modifier(new VitrineTenant.Contenu("x".repeat(161), null, null, null, null, null, false), "sa", LE));
        assertThrows(IllegalArgumentException.class, () -> v.modifier(new VitrineTenant.Contenu("IQ", "a".repeat(601), null, null, null, null, false), "sa", LE));
        List<VitrineTenant.Fonctionnalite> trop = Collections.nCopies(25, new VitrineTenant.Fonctionnalite("F", "T"));
        assertThrows(IllegalArgumentException.class, () -> v.modifier(contenu(trop, null), "sa", LE));
        assertThrows(IllegalArgumentException.class, () -> v.modifier(contenu(List.of(new VitrineTenant.Fonctionnalite(" ", "T")), null), "sa", LE));
        v.modifier(new VitrineTenant.Contenu("IQ", null, null, null, null, null, false), "sa", LE);
        assertEquals(List.of(), v.getFonctionnalites());
        assertNull(v.getAccroche());
    }

    @Test
    void un_chemin_de_media_ne_remonte_jamais_hors_de_la_mediatheque() {
        assertEquals("aide/e2e-cb-iq-01.mp4", VitrineTenant.chemin(" aide/e2e-cb-iq-01.mp4 "));
        assertNull(VitrineTenant.chemin(null));
        assertNull(VitrineTenant.chemin("  "));
        for (String mauvais : new String[]{"../secret", "aide/../../x.mp4", "/etc/passwd", "Aide/X.mp4", "aide//x.mp4", "a".repeat(256)}) {
            assertThrows(IllegalArgumentException.class, () -> VitrineTenant.chemin(mauvais), mauvais);
        }
    }
}
