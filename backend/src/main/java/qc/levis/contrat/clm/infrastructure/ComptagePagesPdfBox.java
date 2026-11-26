package qc.levis.contrat.clm.infrastructure;

import java.io.IOException;
import java.util.OptionalInt;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import qc.levis.contrat.clm.application.port.ComptagePages;
import qc.levis.contrat.clm.domain.FormatDocument;

/** Pages d'un PDF comptées par PDFBox (AF-49) ; un document Word ou un PDF illisible n'est pas paginé. */
@Component
public class ComptagePagesPdfBox implements ComptagePages {

    private static final Logger log = LoggerFactory.getLogger(ComptagePagesPdfBox.class);

    @Override
    public OptionalInt pages(FormatDocument.Type type, byte[] contenu) {
        if (type != FormatDocument.Type.PDF) {
            return OptionalInt.empty();
        }
        try (PDDocument doc = PDDocument.load(contenu)) {
            int n = doc.getNumberOfPages();
            return n > 0 ? OptionalInt.of(n) : OptionalInt.empty();
        } catch (IOException e) {
            // Le dépôt a déjà vérifié la signature du format : un PDF illisible ici reste déposé, simplement non paginé.
            log.debug("Pages d'un PDF non comptées : {}", e.getMessage());
            return OptionalInt.empty();
        }
    }
}
