package qc.levis.contrat.clm.infrastructure;

import static org.junit.jupiter.api.Assertions.*;

import com.sun.net.httpserver.HttpServer;
import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.xmpbox.xml.DomXmpParser;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Service de conversion LibreOffice externe (addenda 4, Q22 ; ADR-018). Contre le vrai service : Gotenberg 8, la
 * même image que la production, démarré par Testcontainers — conversion réelle d'un Word en PDF/A-2b déclaré.
 * Les refus du service (statut HTTP, service arrêté, interruption) se vérifient contre un serveur local qui renvoie
 * ces statuts, faute de pouvoir les provoquer à coup sûr sur le service réel.
 */
@Testcontainers(disabledWithoutDocker = true)
class ConversionServiceHttpTest {

    private static final GenericContainer<?> GOTENBERG = new GenericContainer<>("gotenberg/gotenberg:8.17.3")
            // Même délai qu'en production (docker-compose, chart) : à 30 s par défaut, la conversion échoue en 503 sous charge.
            .withCommand("gotenberg", "--api-timeout=120s")
            .withExposedPorts(3000).waitingFor(Wait.forHttp("/health").forStatusCode(200)).withStartupTimeout(Duration.ofMinutes(3));

    @BeforeAll
    static void demarrer() {
        GOTENBERG.start();
    }

    @AfterAll
    static void arreter() {
        GOTENBERG.stop();
    }

    private static String url() {
        return "http://" + GOTENBERG.getHost() + ":" + GOTENBERG.getMappedPort(3000);
    }

    private static byte[] docx(String texte) throws Exception {
        try (XWPFDocument doc = new XWPFDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            var r = doc.createParagraph().createRun();
            r.setBold(true);
            r.setText(texte);
            doc.createTable(2, 2).getRow(1).getCell(1).setText("250 000 $");
            doc.write(out);
            return out.toByteArray();
        }
    }

    @Test
    void le_service_reel_convertit_un_word_en_pdf_a_2b() throws Exception {
        ConversionServiceHttp c = new ConversionServiceHttp(url(), 120);
        assertTrue(c.disponible());
        byte[] pdf = c.versPdf(docx("Convention de financement — Coopérative Agri-Nord inc."));
        try (PDDocument doc = PDDocument.load(pdf)) {
            String texte = new org.apache.pdfbox.text.PDFTextStripper().getText(doc);
            assertTrue(texte.contains("Coopérative Agri-Nord inc.") && texte.contains("250 000 $"), texte);
            var xmp = new DomXmpParser().parse(doc.getDocumentCatalog().getMetadata().exportXMPMetadata());
            assertEquals(2, xmp.getPDFIdentificationSchema().getPart());
            assertEquals("B", xmp.getPDFIdentificationSchema().getConformance());
            assertFalse(doc.getDocumentCatalog().getOutputIntents().isEmpty(), "intention de sortie PDF/A");
        }
    }

    @Test
    void une_indisponibilite_passagere_503_est_reessayee_une_fois() throws Exception {
        java.util.concurrent.atomic.AtomicInteger appels = new java.util.concurrent.atomic.AtomicInteger();
        HttpServer local = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        local.createContext("/forms/libreoffice/convert", ex -> {
            ex.getRequestBody().readAllBytes();
            byte[] corps = (appels.incrementAndGet() == 1 ? "occupé" : "%PDF-1.7").getBytes(StandardCharsets.UTF_8);
            ex.sendResponseHeaders(appels.get() == 1 ? 503 : 200, corps.length);
            ex.getResponseBody().write(corps);
            ex.close();
        });
        local.start();
        try {
            ConversionServiceHttp c = new ConversionServiceHttp("http://127.0.0.1:" + local.getAddress().getPort() + "/", 5);
            assertEquals("%PDF-1.7", new String(c.versPdf(new byte[]{1}), StandardCharsets.UTF_8));
            assertEquals(2, appels.get());
        } finally {
            local.stop(0);
        }
    }

    @Test
    void service_arrete_statut_d_erreur_et_interruption_sont_signales() throws Exception {
        HttpServer local = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        local.createContext("/health", ex -> {
            ex.sendResponseHeaders(503, -1);
            ex.close();
        });
        local.createContext("/forms/libreoffice/convert", ex -> {
            byte[] corps = "LibreOffice indisponible".getBytes(StandardCharsets.UTF_8);
            ex.sendResponseHeaders(503, corps.length);
            ex.getResponseBody().write(corps);
            ex.close();
        });
        local.start();
        try {
            ConversionServiceHttp c = new ConversionServiceHttp("http://127.0.0.1:" + local.getAddress().getPort() + "/", 5);
            assertFalse(c.disponible(), "santé non 200");
            assertTrue(assertThrows(IllegalStateException.class, () -> c.versPdf(new byte[]{1})).getMessage().contains("HTTP 503"));
            Thread.currentThread().interrupt();
            try {
                assertFalse(c.disponible(), "sondage interrompu");
            } finally {
                Thread.interrupted();
            }
            Thread.currentThread().interrupt();
            try {
                assertTrue(assertThrows(IllegalStateException.class, () -> c.versPdf(new byte[]{1})).getMessage().contains("interrompue"));
            } finally {
                Thread.interrupted();
            }
        } finally {
            local.stop(0);
        }
        ConversionServiceHttp arrete = new ConversionServiceHttp("http://127.0.0.1:1", 5);
        assertFalse(arrete.disponible());
        assertTrue(assertThrows(IllegalStateException.class, () -> arrete.versPdf(new byte[]{1})).getMessage().contains("injoignable"));
    }

    @Test
    void le_formulaire_suit_la_rfc_7578() {
        String corps = new String(ConversionServiceHttp.formulaire("XYZ", "PK".getBytes()), StandardCharsets.UTF_8);
        assertTrue(corps.startsWith("--XYZ\r\nContent-Disposition: form-data; name=\"files\"; filename=\"document.docx\"\r\n"));
        assertTrue(corps.contains("\r\n\r\nPK\r\n--XYZ\r\nContent-Disposition: form-data; name=\"pdfa\"\r\n\r\nPDF/A-2b\r\n--XYZ--\r\n"));
    }
}
