package qc.levis.contrat.clm.infrastructure;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Conversion Word → PDF/A par un {@code soffice} simulé (script shell) : mêmes arguments que LibreOffice, mêmes issues
 * — succès, échec, absence de résultat, délai dépassé —, sans installer LibreOffice sur le poste de construction.
 */
class ConversionLibreOfficeTest {

    @TempDir
    Path dossier;

    private String script(String corps) throws Exception {
        Path s = dossier.resolve("soffice-" + System.nanoTime());
        Files.writeString(s, "#!/bin/sh\n" + corps + "\n");
        Files.setPosixFilePermissions(s, PosixFilePermissions.fromString("rwxr-xr-x"));
        return s.toString();
    }

    /** Écrit « %PDF-simule » dans le répertoire --outdir, sous le nom du document source, comme LibreOffice. */
    private static final String CONVERTIT = """
            [ "$1" = "--version" ] && echo "LibreOffice 24.2" && exit 0
            while [ $# -gt 1 ]; do
              case "$1" in --outdir) sortie="$2" ;; --convert-to) filtre="$2" ;; esac
              shift
            done
            case "$filtre" in *SelectPdfVersion*) ;; *) exit 9 ;; esac
            printf '%%PDF-simule' > "$sortie/$(basename "$1" .docx).pdf"
            """;

    @Test
    void un_soffice_present_convertit_en_pdf_a_dans_un_repertoire_jetable() throws Exception {
        ConversionLibreOffice c = new ConversionLibreOffice(script(CONVERTIT), 30);
        assertTrue(c.disponible());
        assertTrue(c.disponible(), "sondé une fois");
        assertEquals("%PDF-simule", new String(c.versPdf("PK".getBytes())));
        try (var restes = Files.list(Path.of(System.getProperty("java.io.tmpdir")))) {
            assertTrue(restes.noneMatch(p -> p.getFileName().toString().startsWith("clm-conversion-") && Files.exists(p.resolve("document.docx"))),
                    "répertoire de conversion effacé");
        }
    }

    @Test
    void un_soffice_absent_est_indisponible() {
        assertFalse(new ConversionLibreOffice(dossier.resolve("absent").toString(), 30).disponible());
        assertTrue(assertThrows(IllegalStateException.class, () -> new ConversionLibreOffice(dossier.resolve("absent").toString(), 30)
                .versPdf("PK".getBytes())).getMessage().contains("impossible"));
    }

    @Test
    void echec_absence_de_resultat_et_delai_depasse_sont_signales() throws Exception {
        assertFalse(new ConversionLibreOffice(script("exit 2"), 30).disponible(), "--version en échec");
        assertFalse(new ConversionLibreOffice(script("sleep 5"), 1).disponible(), "--version sans réponse dans le délai");
        assertTrue(assertThrows(IllegalStateException.class, () -> new ConversionLibreOffice(script("exit 3"), 30).versPdf("PK".getBytes()))
                .getMessage().contains("code 3"));
        assertTrue(assertThrows(IllegalStateException.class, () -> new ConversionLibreOffice(script("exit 0"), 30).versPdf("PK".getBytes()))
                .getMessage().contains("sans résultat"));
        assertTrue(assertThrows(IllegalStateException.class, () -> new ConversionLibreOffice(script("sleep 5"), 1).versPdf("PK".getBytes()))
                .getMessage().contains("délai"));
        ConversionLibreOffice lent = new ConversionLibreOffice(script("sleep 5"), 30);
        Thread.currentThread().interrupt();
        try {
            assertTrue(assertThrows(IllegalStateException.class, () -> lent.versPdf("PK".getBytes())).getMessage().contains("interrompue"));
        } finally {
            Thread.interrupted();
        }
        Thread.currentThread().interrupt();
        try {
            assertFalse(new ConversionLibreOffice(script("sleep 5"), 30).disponible(), "sondage interrompu");
        } finally {
            Thread.interrupted();
        }
    }
}
