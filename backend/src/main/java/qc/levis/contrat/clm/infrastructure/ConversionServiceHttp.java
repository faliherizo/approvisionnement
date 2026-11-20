package qc.levis.contrat.clm.infrastructure;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import qc.levis.contrat.clm.application.port.ConversionPdf;

/**
 * Conversion Word → PDF/A-2b par un service LibreOffice externe exposant l'API Gotenberg 8
 * ({@code POST /forms/libreoffice/convert}, champ {@code files}, {@code pdfa=PDF/A-2b} ; {@code GET /health}). Le
 * service tourne hors du serveur : LibreOffice, qui analyse des fichiers fournis par des tiers, y est isolé, et le
 * même conteneur sert la production et les bacs à sable (ADR-018). Retenu par {@code app.clm.conversion.mode=service}.
 */
@Component
@ConditionalOnProperty(name = "app.clm.conversion.mode", havingValue = "service")
public class ConversionServiceHttp implements ConversionPdf {

    static final String MIME_DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

    private final URI base;
    private final Duration delai;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    public ConversionServiceHttp(@Value("${app.clm.conversion.url}") String url,
                                 @Value("${app.clm.conversion.delai-secondes:120}") long delaiSecondes) {
        this.base = URI.create(url.endsWith("/") ? url : url + "/");
        this.delai = Duration.ofSeconds(delaiSecondes);
    }

    /** Interrogé à chaque génération : un service redémarré redevient disponible sans redémarrer le serveur. */
    @Override
    public boolean disponible() {
        try {
            return http.send(HttpRequest.newBuilder(base.resolve("health")).timeout(Duration.ofSeconds(5)).GET().build(),
                    HttpResponse.BodyHandlers.discarding()).statusCode() == 200;
        } catch (IOException e) {
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    @Override
    public byte[] versPdf(byte[] docx) {
        String frontiere = "----clm" + UUID.randomUUID();
        try {
            HttpResponse<byte[]> r = http.send(HttpRequest.newBuilder(base.resolve("forms/libreoffice/convert")).timeout(delai)
                    .header("Content-Type", "multipart/form-data; boundary=" + frontiere)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(formulaire(frontiere, docx))).build(),
                    HttpResponse.BodyHandlers.ofByteArray());
            if (r.statusCode() != 200) {
                throw new IllegalStateException("Conversion PDF refusée par le service (HTTP " + r.statusCode() + ") : "
                        + new String(r.body(), StandardCharsets.UTF_8).strip());
            }
            return r.body();
        } catch (IOException e) {
            throw new IllegalStateException("Service de conversion injoignable : " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Conversion PDF interrompue.", e);
        }
    }

    /** Corps multipart/form-data (RFC 7578) : le document, puis la conformité PDF/A demandée. */
    static byte[] formulaire(String frontiere, byte[] docx) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        String debut = "--" + frontiere + "\r\nContent-Disposition: form-data; name=\"files\"; filename=\"document.docx\"\r\n"
                + "Content-Type: " + MIME_DOCX + "\r\n\r\n";
        out.writeBytes(debut.getBytes(StandardCharsets.UTF_8));
        out.writeBytes(docx);
        String fin = "\r\n--" + frontiere + "\r\nContent-Disposition: form-data; name=\"pdfa\"\r\n\r\nPDF/A-2b\r\n--" + frontiere + "--\r\n";
        out.writeBytes(fin.getBytes(StandardCharsets.UTF_8));
        return out.toByteArray();
    }
}
