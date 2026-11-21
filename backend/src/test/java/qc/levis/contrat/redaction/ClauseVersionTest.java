package qc.levis.contrat.redaction;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.commun.domain.Enums;
import qc.levis.contrat.redaction.domain.Clause;
import qc.levis.contrat.redaction.domain.ClauseVersion;
import qc.levis.contrat.redaction.domain.ClauseVersion.Motif;

class ClauseVersionTest {

    private static final Instant T0 = Instant.parse("2026-11-20T10:00:00Z");
    private static final Instant T1 = Instant.parse("2026-11-21T10:00:00Z");

    private static Clause clause() {
        Clause c = new Clause();
        c.setTenantId(1L);
        c.setTitre("Assurance");
        c.setCategorie(Enums.ClauseCategorie.STANDARD);
        c.setCorps("Texte v1");
        c.setNumero("12.1");
        c.setSection("Assurances");
        c.setNormative(true);
        return c;
    }

    @Test
    void la_version_fige_le_texte_les_attributs_l_auteur_et_la_date() {
        ClauseVersion v = ClauseVersion.de(clause(), Motif.CREATION, null, "a@levis.qc.ca", T0);
        assertEquals(1, v.getNumero());
        assertEquals(1L, v.getTenantId());
        assertEquals("Assurance", v.getTitre());
        assertEquals(Enums.ClauseCategorie.STANDARD, v.getCategorie());
        assertEquals("Texte v1", v.getCorps());
        assertEquals("12.1", v.getArticle());
        assertEquals("Assurances", v.getSection());
        assertTrue(v.isNormative());
        assertEquals(Motif.CREATION, v.getMotif());
        assertNull(v.getVersionRestauree());
        assertEquals("a@levis.qc.ca", v.getAuteur());
        assertEquals(T0, v.getCreeLe());
        assertNull(v.getId());
        assertNull(v.getClauseId());
    }

    @Test
    void restaurer_reporte_le_contenu_et_fait_avancer_la_version() {
        Clause c = clause();
        ClauseVersion v1 = ClauseVersion.de(c, Motif.CREATION, null, "a", T0);
        c.setCorps("Texte v2");
        c.setTitre("Autre");
        c.setCategorie(Enums.ClauseCategorie.PERSONNALISEE);
        c.setNumero(null);
        c.setSection(null);
        c.setNormative(false);
        c.setVersion(2);
        v1.restaurerDans(c, T1);
        assertEquals("Texte v1", c.getCorps());
        assertEquals("Assurance", c.getTitre());
        assertEquals(Enums.ClauseCategorie.STANDARD, c.getCategorie());
        assertEquals("12.1", c.getNumero());
        assertEquals("Assurances", c.getSection());
        assertTrue(c.isNormative());
        assertEquals(3, c.getVersion());
        assertEquals(T1, c.getMajLe());
        assertEquals(2, ClauseVersion.de(c, Motif.RESTAURATION, 1, "b", T1).getVersionRestauree() + 1);
    }
}
