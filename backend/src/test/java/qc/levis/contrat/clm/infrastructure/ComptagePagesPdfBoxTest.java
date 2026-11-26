package qc.levis.contrat.clm.infrastructure;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.OptionalInt;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.clm.domain.FormatDocument;

/** Port ComptagePages (AF-49) : pages d'un PDF comptées ; Word, PDF illisible ou sans page : non paginé, jamais d'erreur. */
class ComptagePagesPdfBoxTest {

    private final ComptagePagesPdfBox comptage = new ComptagePagesPdfBox();

    /** PDF de {@code n} pages produit par PDFBox. */
    static byte[] pdf(int n) throws IOException {
        try (PDDocument d = new PDDocument(); ByteArrayOutputStream o = new ByteArrayOutputStream()) {
            for (int i = 0; i < n; i++) {
                d.addPage(new PDPage());
            }
            d.save(o);
            return o.toByteArray();
        }
    }

    @Test
    void compte_les_pages_d_un_pdf() throws IOException {
        assertEquals(OptionalInt.of(1), comptage.pages(FormatDocument.Type.PDF, pdf(1)));
        assertEquals(OptionalInt.of(7), comptage.pages(FormatDocument.Type.PDF, pdf(7)));
    }

    @Test
    void un_document_word_un_pdf_illisible_ou_sans_page_n_est_pas_pagine() throws IOException {
        assertEquals(OptionalInt.empty(), comptage.pages(FormatDocument.Type.DOCX, pdf(2)), "seul le PDF se pagine");
        assertEquals(OptionalInt.empty(), comptage.pages(FormatDocument.Type.PDF, "%PDF-1.4 tronqué".getBytes(StandardCharsets.US_ASCII)));
        assertEquals(OptionalInt.empty(), comptage.pages(FormatDocument.Type.PDF, pdf(0)));
    }
}
