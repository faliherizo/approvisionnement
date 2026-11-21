package qc.levis.contrat.document.infrastructure.web;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

/** Une pièce jointe n'est jamais servie avec un type interprétable par le navigateur (HTML, SVG, script). */
class DocumentDownloadControllerTest {
    @Test
    void seuls_les_types_surs_sont_servis_tels_quels() {
        assertEquals(MediaType.APPLICATION_PDF, DocumentDownloadController.typeServi("application/pdf"));
        assertEquals(MediaType.IMAGE_PNG, DocumentDownloadController.typeServi("IMAGE/PNG; charset=x"));
        assertEquals(MediaType.APPLICATION_OCTET_STREAM, DocumentDownloadController.typeServi("text/html"));
        assertEquals(MediaType.APPLICATION_OCTET_STREAM, DocumentDownloadController.typeServi("image/svg+xml"));
        assertEquals(MediaType.APPLICATION_OCTET_STREAM, DocumentDownloadController.typeServi("n'importe quoi/;;"));
        assertEquals(MediaType.APPLICATION_OCTET_STREAM, DocumentDownloadController.typeServi(null));
    }
}
