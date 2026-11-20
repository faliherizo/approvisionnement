package qc.levis.contrat.clm.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/** Gabarit Word d'un modèle (addenda 4, Q22) : brouillon seulement, Word seulement, variables relevées au dépôt. */
class GabaritWordTest {

    private static final Instant T0 = Instant.parse("2026-11-20T12:00:00Z");
    private static final byte[] DOCX = {'P', 'K', 3, 4, 1, 2};

    private static ModeleContrat brouillon() {
        ModeleContrat m = new ModeleContrat(1L, "cle", 1, "Convention", "redacteur@iq.example", T0);
        ReflectionTestUtils.setField(m, "id", 7L);
        return m;
    }

    private static GabaritWord.Depot depot(String nom, byte[] contenu, String... variables) {
        return new GabaritWord.Depot(nom, contenu, new LinkedHashSet<>(List.of(variables)));
    }

    @Test
    void un_gabarit_se_depose_sur_un_brouillon_et_suit_le_modele_a_la_version_suivante() {
        GabaritWord g = new GabaritWord(brouillon(), depot("../Convention IQ.docx", DOCX, "montant", "clauses", "inconnue"), "redacteur", T0);
        assertEquals(7L, g.getModeleId());
        assertEquals(1L, g.getTenantId());
        assertEquals("Convention IQ.docx", g.getNom(), "nom assaini");
        assertArrayEquals(DOCX, g.getContenu());
        assertEquals("redacteur", g.getDeposePar());
        assertEquals(T0, g.getDeposeLe());
        assertEquals(Set.of("montant", "clauses", "inconnue"), g.variables());
        assertEquals(List.of("inconnue"), g.inconnues(Set.of("montant")), "le marqueur de clauses est toujours admis");
        g.getContenu()[0] = 0;
        assertEquals('P', g.getContenu()[0], "copie défensive");

        ModeleContrat suivante = new ModeleContrat(1L, "cle", 2, "Convention", "redacteur@iq.example", T0.plusSeconds(60));
        ReflectionTestUtils.setField(suivante, "id", 8L);
        GabaritWord copie = g.pour(suivante, T0.plusSeconds(60));
        assertEquals(8L, copie.getModeleId());
        assertEquals(g.variables(), copie.variables());
        assertEquals(Set.of(), new GabaritWord(brouillon(), depot("vide.docx", DOCX), "r", T0).variables());
    }

    @Test
    void seuls_un_brouillon_et_un_document_word_sont_admis() {
        ModeleContrat publie = brouillon();
        publie.changerStatut(Modele.Statut.EN_REVISION, T0);
        assertThrows(IllegalStateException.class, () -> new GabaritWord(publie, depot("c.docx", DOCX), "r", T0));
        assertThrows(IllegalArgumentException.class, () -> new GabaritWord(brouillon(), depot("c.pdf", "%PDF-1.7".getBytes()), "r", T0));
        assertThrows(IllegalArgumentException.class, () -> new GabaritWord(brouillon(), depot("c.docx", "texte".getBytes()), "r", T0));
        assertThrows(IllegalArgumentException.class, () -> new GabaritWord(brouillon(), depot("c.docx", new byte[(int) GabaritWord.TAILLE_MAX + 1]), "r", T0));
        String[] beaucoup = new String[500];
        for (int i = 0; i < beaucoup.length; i++) {
            beaucoup[i] = "variable_" + i;
        }
        assertTrue(assertThrows(IllegalArgumentException.class, () -> new GabaritWord(brouillon(), depot("c.docx", DOCX, beaucoup), "r", T0))
                .getMessage().contains("trop de variables"));
    }
}
