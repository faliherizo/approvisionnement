package qc.levis.contrat.redaction.infrastructure;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.springframework.stereotype.Service;
import qc.levis.contrat.addenda.domain.Addenda;
import qc.levis.contrat.redaction.domain.Document;
import qc.levis.contrat.redaction.domain.DocumentAnnexe;
import qc.levis.contrat.redaction.domain.DocumentClause;
import qc.levis.contrat.soumission.domain.Bordereau;
import qc.levis.contrat.soumission.domain.LignePrix;

/**
 * Compose un {@link ContractModel} à partir d'un document, de ses addendas, annexes et
 * bordereau. Responsabilité unique (SRP) : traduire les entités en modèle de rendu,
 * incluant la numérotation hiérarchique et le regroupement en sections.
 */
@Service
public class ContractComposer {

    private static final DateTimeFormatter DATE =
            DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.of("America/Toronto"));

    public ContractModel composer(Document doc, List<Addenda> addendas,
                                  List<DocumentAnnexe> annexesDoc, Bordereau bordereau) {
        return composer(doc, addendas, annexesDoc, bordereau, GabaritRendu.defaut());
    }

    /** Compose selon un gabarit de rendu explicite (couverture, corps, marges). */
    public ContractModel composer(Document doc, List<Addenda> addendas,
                                  List<DocumentAnnexe> annexesDoc, Bordereau bordereau, GabaritRendu gabarit) {
        return new ContractModel(
                nz(doc.getTitre()),
                doc.getNumeroAO(),
                doc.getType() != null ? doc.getType().name() : "",
                doc.getStatut() != null ? doc.getStatut().name() : "",
                doc.getCreeLe() != null ? DATE.format(doc.getCreeLe()) : "",
                doc.getOrganismePublic(),
                doc.getPrestataire(),
                texte(doc.getContexte()),
                sections(doc),
                annexes(annexesDoc, addendas),
                bordereau(bordereau),
                gabarit);
    }

    /** Regroupe les clauses par section (ordre préservé). */
    private List<ContractModel.Section> sections(Document doc) {
        Map<String, List<ContractModel.Article>> parSection = new LinkedHashMap<>();
        int seq = 1;
        for (DocumentClause dc : doc.getClauses()) {
            String numero = (dc.getNumero() != null && !dc.getNumero().isBlank())
                    ? dc.getNumero() : String.format("%d.00", seq);
            String section = (dc.getSection() != null && !dc.getSection().isBlank())
                    ? dc.getSection() : "Clauses générales";
            parSection.computeIfAbsent(section, k -> new ArrayList<>())
                    .add(new ContractModel.Article(numero, nz(dc.getTitre()), texte(dc.getTexteEffectif())));
            seq++;
        }
        List<ContractModel.Section> sections = new ArrayList<>();
        parSection.forEach((titre, articles) -> sections.add(new ContractModel.Section(titre, articles)));
        return sections;
    }

    /** Annexes : annexes déclarées + addendas émis (numérotés). */
    private List<ContractModel.Annexe> annexes(List<DocumentAnnexe> annexesDoc, List<Addenda> addendas) {
        List<ContractModel.Annexe> res = new ArrayList<>();
        if (annexesDoc != null) {
            for (DocumentAnnexe a : annexesDoc) {
                String num = (a.getNumero() != null && !a.getNumero().isBlank())
                        ? a.getNumero() : "ANNEXE";
                List<String> elements = new ArrayList<>();
                if (a.getReferenceClause() != null && !a.getReferenceClause().isBlank()) {
                    elements.add("Se rapporte à la clause " + a.getReferenceClause());
                }
                if (a.getContenu() != null && !a.getContenu().isBlank()) elements.add(texte(a.getContenu()));
                res.add(new ContractModel.Annexe(num, nz(a.getTitre()), elements));
            }
        }
        if (addendas != null) {
            addendas.stream().filter(a -> a.getEmisLe() != null).forEach(a -> {
                List<String> elements = new ArrayList<>();
                a.getChangements().forEach(ch -> elements.add(nz(ch.getCode()) + " : " + texte(ch.getNouveauTexte())));
                res.add(new ContractModel.Annexe("Addenda n°" + a.getNumero(), nz(a.getObjet()), elements));
            });
        }
        return res;
    }

    private ContractModel.Bordereau bordereau(Bordereau b) {
        if (b == null || b.getLignes().isEmpty()) return null;
        List<ContractModel.Bordereau.Ligne> lignes = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (LignePrix l : b.getLignes()) {
            total = total.add(l.getTotal());
            lignes.add(new ContractModel.Bordereau.Ligne(
                    nz(l.getDescription()), montant(l.getQuantite()), montant(l.getTaux()), montant(l.getTotal())));
        }
        return new ContractModel.Bordereau(b.getTitre() != null ? b.getTitre() : "Bordereau de prix",
                lignes, montant(total));
    }

    private String montant(BigDecimal v) { return v == null ? "" : v.stripTrailingZeros().toPlainString(); }

    /** Convertit un contenu HTML (WYSIWYG) en texte simple pour le rendu. */
    private String texte(String s) {
        if (s == null) return "";
        return s.replaceAll("(?i)<\\s*br\\s*/?>", "\n")
                .replaceAll("(?i)</\\s*(p|div|li|h4)\\s*>", "\n")
                .replaceAll("(?i)<\\s*li[^>]*>", "• ")
                .replaceAll("<[^>]+>", "")
                .replace("&nbsp;", " ").replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">")
                .replaceAll("\n{3,}", "\n\n").trim();
    }

    private String nz(String s) { return s == null ? "" : s; }
}
