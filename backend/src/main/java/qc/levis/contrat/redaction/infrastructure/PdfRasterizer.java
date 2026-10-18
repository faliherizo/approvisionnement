package qc.levis.contrat.redaction.infrastructure;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import javax.imageio.ImageIO;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Component;

/**
 * Rasterise une page d'un PDF en image PNG (via PDFBox). Sert à l'aperçu affiché à l'écran : une
 * image se rend dans tous les navigateurs et dans la captation vidéo, contrairement au lecteur PDF
 * natif embarqué qui reste vide en environnement sans module PDF.
 */
@Component
public class PdfRasterizer {

    /** Rend la page {@code index} (0-based, bornée) du PDF en PNG, à la résolution donnée. */
    public byte[] pagePng(byte[] pdf, int index, int dpi) {
        try (PDDocument document = PDDocument.load(pdf)) {
            int page = Math.max(0, Math.min(index, document.getNumberOfPages() - 1));
            BufferedImage image = new PDFRenderer(document).renderImageWithDPI(page, dpi, ImageType.RGB);
            ByteArrayOutputStream sortie = new ByteArrayOutputStream();
            ImageIO.write(image, "png", sortie);
            return sortie.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Rasterisation de l'aperçu PDF impossible", e);
        }
    }
}
