package qc.levis.contrat.saisie.infrastructure;

import java.time.Clock;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import org.springframework.stereotype.Component;
import qc.levis.contrat.commun.infrastructure.pdf.DocumentPdfA;
import qc.levis.contrat.commun.infrastructure.pdf.DocumentPdfA.Bloc;
import qc.levis.contrat.saisie.application.port.RenduFiche;
import qc.levis.contrat.saisie.domain.ContenuFiche;

/** Document PDF/A-2b d'une fiche : identification, puis chaque page du formulaire avec ses questions et réponses. */
@Component
class RenduFichePdf implements RenduFiche {

    static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.of("America/Toronto"));

    private final Clock horloge;

    RenduFichePdf(Clock horloge) { this.horloge = horloge; }

    @Override
    public byte[] pdf(ContenuFiche c) {
        List<Bloc> blocs = new ArrayList<>(List.of(
                new DocumentPdfA.Paire("Fiche", c.numero()),
                new DocumentPdfA.Paire("Statut", c.statut()),
                new DocumentPdfA.Paire("Soumise le", c.soumisLe() == null ? "—" : DATE.format(c.soumisLe())),
                new DocumentPdfA.Paire("Demandeur", c.demandeur())));
        String page = null;
        for (ContenuFiche.Ligne l : c.lignes()) {
            if (!l.page().equals(page)) blocs.add(new DocumentPdfA.Titre(l.page().isBlank() ? " " : l.page()));
            page = l.page();
            blocs.add(new DocumentPdfA.Paire(l.libelle(), l.valeur()));
        }
        return DocumentPdfA.produire(c.formulaire(), blocs, GregorianCalendar.from(horloge.instant().atZone(ZoneId.of("UTC"))));
    }
}
