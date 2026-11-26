package qc.levis.contrat.clm.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Annexe 10 IQ, AF-49 (RST-51) : comptage des documents et des pages signés, par unité et par mois. */
class ComptageSignesTest {

    private static final ZoneId QUEBEC = ZoneId.of("America/Toronto");

    @Test
    void compte_documents_et_pages_au_total_par_unite_et_par_mois_sans_double_comptage() {
        DocumentSigne a = new DocumentSigne(1L, "FINANCEMENT", Instant.parse("2026-10-31T12:00:00Z"), 3);
        DocumentSigne b = new DocumentSigne(2L, "FINANCEMENT", Instant.parse("2026-11-02T12:00:00Z"), 5);
        DocumentSigne word = new DocumentSigne(3L, null, Instant.parse("2026-11-03T12:00:00Z"), null);
        // 1er novembre 02:00 UTC = 31 octobre 22:00 à Québec : le mois suit le fuseau de l'organisation.
        DocumentSigne soir = new DocumentSigne(4L, "ACHATS", Instant.parse("2026-11-01T02:00:00Z"), 2);

        ComptageSignes.Releve r = ComptageSignes.compter(List.of(a, b, word, soir, a), QUEBEC);

        assertEquals(new ComptageSignes.Compte(4, 10, 1), r.total(), "le document cité deux fois compte une fois");
        assertEquals(List.of("", "ACHATS", "FINANCEMENT"), List.copyOf(r.parUnite().keySet()), "sans unité d'abord, puis par ordre alphabétique");
        assertEquals(new ComptageSignes.Compte(1, 0, 1), r.parUnite().get(ComptageSignes.SANS_UNITE));
        assertEquals(new ComptageSignes.Compte(2, 8, 0), r.parUnite().get("FINANCEMENT"));
        assertEquals(Map.of(YearMonth.of(2026, 10), new ComptageSignes.Compte(2, 5, 0),
                YearMonth.of(2026, 11), new ComptageSignes.Compte(2, 5, 1)), r.parMois());
    }

    @Test
    void une_periode_sans_document_signe_compte_zero() {
        ComptageSignes.Releve r = ComptageSignes.compter(List.of(), QUEBEC);
        assertEquals(ComptageSignes.Compte.ZERO, r.total());
        assertTrue(r.parUnite().isEmpty());
        assertTrue(r.parMois().isEmpty());
    }

    @Test
    void un_document_se_pagine_d_au_moins_une_page() {
        DocumentTrousse d = new DocumentTrousse(1L, 2L, "c", 1, "c.pdf", FormatDocument.Type.PDF, new byte[]{1}, "a", null,
                Instant.parse("2026-11-26T00:00:00Z"));
        assertNull(d.getPages(), "non paginé tant que le comptage n'a pas eu lieu");
        d.paginer(7);
        assertEquals(7, d.getPages());
        assertThrows(IllegalArgumentException.class, () -> d.paginer(0));
        assertEquals(7, d.getPages());
    }
}
