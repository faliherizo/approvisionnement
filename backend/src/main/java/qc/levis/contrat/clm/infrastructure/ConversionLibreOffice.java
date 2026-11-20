package qc.levis.contrat.clm.infrastructure;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;
import qc.levis.contrat.clm.application.port.ConversionPdf;

/**
 * Conversion Word → PDF/A-2b par LibreOffice sans interface ({@code soffice --headless}), installé dans l'image du
 * serveur. Chaque conversion s'exécute dans un répertoire temporaire et un profil utilisateur propres, détruits ensuite :
 * aucune conversion ne voit le document d'une autre, et deux conversions simultanées ne se bloquent pas.
 */
@Component
public class ConversionLibreOffice implements ConversionPdf {

    private static final Logger log = LoggerFactory.getLogger(ConversionLibreOffice.class);
    /** Filtre d'export Writer : version PDF/A-2 (SelectPdfVersion = 2), polices intégrées. */
    static final String FILTRE = "pdf:writer_pdf_Export:{\"SelectPdfVersion\":{\"type\":\"long\",\"value\":\"2\"}}";

    private final String executable;
    private final Duration delai;
    private Boolean disponible;

    public ConversionLibreOffice(@Value("${app.clm.conversion.soffice:soffice}") String executable,
                                 @Value("${app.clm.conversion.delai-secondes:120}") long delaiSecondes) {
        this.executable = executable;
        this.delai = Duration.ofSeconds(delaiSecondes);
    }

    @Override
    public synchronized boolean disponible() {
        if (disponible == null) {
            disponible = sonder();
            log.info("Conversion Word → PDF/A par LibreOffice : {}", disponible ? "disponible" : "indisponible (" + executable + ")");
        }
        return disponible;
    }

    private boolean sonder() {
        try {
            Process p = new ProcessBuilder(executable, "--version").redirectErrorStream(true)
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD).start();
            if (!p.waitFor(delai.toSeconds(), TimeUnit.SECONDS)) {
                p.destroyForcibly();
                return false;
            }
            return p.exitValue() == 0;
        } catch (IOException e) {
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    @Override
    public byte[] versPdf(byte[] docx) {
        Path dossier = null;
        try {
            dossier = Files.createTempDirectory("clm-conversion-");
            Path source = Files.write(dossier.resolve("document.docx"), docx);
            executer(dossier, source);
            Path pdf = dossier.resolve("document.pdf");
            if (!Files.exists(pdf)) {
                throw new IllegalStateException("Conversion PDF sans résultat.");
            }
            return Files.readAllBytes(pdf);
        } catch (IOException e) {
            throw new IllegalStateException("Conversion PDF impossible : " + e.getMessage(), e);
        } finally {
            effacer(dossier);
        }
    }

    private void executer(Path dossier, Path source) throws IOException {
        List<String> commande = List.of(executable, "--headless", "--norestore", "--nolockcheck",
                "-env:UserInstallation=" + dossier.resolve("profil").toUri(), "--convert-to", FILTRE,
                "--outdir", dossier.toString(), source.toString());
        Process p = new ProcessBuilder(commande).redirectErrorStream(true).redirectOutput(dossier.resolve("journal.txt").toFile()).start();
        try {
            if (!p.waitFor(delai.toSeconds(), TimeUnit.SECONDS)) {
                p.destroyForcibly();
                throw new IllegalStateException("Conversion PDF interrompue : délai de " + delai.toSeconds() + " s dépassé.");
            }
            if (p.exitValue() != 0) {
                throw new IllegalStateException("Conversion PDF en échec (code " + p.exitValue() + ").");
            }
        } catch (InterruptedException e) {
            p.destroyForcibly();
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Conversion PDF interrompue.", e);
        }
    }

    /** Le document et le profil LibreOffice ne survivent pas à la conversion. */
    private static void effacer(Path dossier) {
        if (dossier != null && !FileSystemUtils.deleteRecursively(dossier.toFile())) {
            log.warn("Répertoire de conversion non effacé : {}", dossier);
        }
    }
}
