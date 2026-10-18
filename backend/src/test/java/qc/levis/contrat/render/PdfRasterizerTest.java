package qc.levis.contrat.render;

import java.io.ByteArrayOutputStream;
import java.io.UncheckedIOException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.redaction.infrastructure.PdfRasterizer;
import static org.junit.jupiter.api.Assertions.*;

/** Tests unitaires de la rasterisation d'aperçu PDF → PNG. */
class PdfRasterizerTest {

    private final PdfRasterizer rasterizer = new PdfRasterizer();

    private byte[] pdfUnePage() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (PDDocument doc = new PDDocument()) {
            doc.addPage(new PDPage());
            doc.save(out);
        }
        return out.toByteArray();
    }

    @Test
    void pagePng_produitUneImagePng() throws Exception {
        byte[] png = rasterizer.pagePng(pdfUnePage(), 0, 96);
        assertTrue(png.length > 0);
        // Signature PNG : 0x89 'P' 'N' 'G'.
        assertEquals((byte) 0x89, png[0]);
        assertEquals('P', png[1]);
        assertEquals('N', png[2]);
        assertEquals('G', png[3]);
    }

    @Test
    void pagePng_indexHorsBornes_estBorne() throws Exception {
        // Page 9 sur un document d'une page → borné à la page 0, sans erreur.
        assertTrue(rasterizer.pagePng(pdfUnePage(), 9, 96).length > 0);
    }

    @Test
    void pagePng_pdfInvalide_leveUncheckedIO() {
        assertThrows(UncheckedIOException.class,
                () -> rasterizer.pagePng(new byte[]{1, 2, 3}, 0, 96));
    }
}
