package qc.levis.contrat.api;

import java.io.IOException;
import java.io.UncheckedIOException;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;
import qc.levis.contrat.redaction.application.ImportService;
import qc.levis.contrat.redaction.infrastructure.web.ImportController;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Test unitaire ciblé : lecture impossible d'un fichier Word déposé → erreur enveloppée. */
class ImportControllerTest {

    @Test
    void importerWord_fichierIllisible_enveloppeIOException() throws Exception {
        ImportController controller = new ImportController(mock(ImportService.class));
        MultipartFile fichier = mock(MultipartFile.class);
        when(fichier.getBytes()).thenThrow(new IOException("flux interrompu"));

        assertThrows(UncheckedIOException.class,
                () -> controller.importerWord(fichier, true, "X", null, null));
    }
}
