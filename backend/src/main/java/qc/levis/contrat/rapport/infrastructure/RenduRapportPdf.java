package qc.levis.contrat.rapport.infrastructure;

import java.time.Clock;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import qc.levis.contrat.commun.infrastructure.pdf.DocumentPdfA;
import qc.levis.contrat.commun.infrastructure.pdf.DocumentPdfA.Bloc;
import qc.levis.contrat.rapport.application.port.RenduRapport;
import qc.levis.contrat.rapport.domain.RapportFiches;

/** Rapport PDF/A-2b : formulaire, filtre, décompte par statut, puis le tableau des fiches avec les colonnes choisies. */
@Component
class RenduRapportPdf implements RenduRapport {

    private final Clock horloge;

    RenduRapportPdf(Clock horloge) { this.horloge = horloge; }

    @Override
    public byte[] pdf(RapportFiches r) {
        List<Bloc> blocs = new ArrayList<>(List.of(
                new DocumentPdfA.Paire("Formulaire", r.formulaire()),
                new DocumentPdfA.Paire("Statuts retenus", r.statutsRetenus().isEmpty() ? "Tous" : String.join(", ", r.statutsRetenus())),
                new DocumentPdfA.Paire("Fiches", String.valueOf(r.total())),
                new DocumentPdfA.Titre("Décompte par statut")));
        List<List<String>> decompte = r.parStatut().entrySet().stream().sorted(Map.Entry.comparingByKey())
                .map(e -> List.of(e.getKey(), String.valueOf(e.getValue()))).toList();
        blocs.add(new DocumentPdfA.Tableau(List.of("Statut", "Fiches"), decompte));
        blocs.add(new DocumentPdfA.Titre("Fiches"));
        blocs.add(new DocumentPdfA.Tableau(r.colonnes(), r.lignes()));
        return DocumentPdfA.produire(r.titre(), blocs, GregorianCalendar.from(horloge.instant().atZone(ZoneId.of("UTC"))));
    }
}
