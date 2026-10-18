package qc.levis.contrat.redaction.infrastructure;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Component;

/**
 * Parseur de modèle Word (.docx) — adaptateur d'infrastructure basé sur Apache POI (ADR-008).
 * Découpe le document en sections {titre, corps} : un paragraphe de <em>style titre</em>
 * (Heading/Titre) ou <em>entièrement en gras</em> ouvre une nouvelle section ; les paragraphes
 * suivants en forment le corps. Sert à migrer les modèles Word existants de la Ville (exig. 3, 16).
 */
@Component
public class WordTemplateParser {

    /** Section extraite d'un document Word. */
    public record SectionWord(String titre, String corps) {}

    public List<SectionWord> extraire(byte[] contenu) {
        List<SectionWord> sections = new ArrayList<>();
        try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(contenu))) {
            String titreCourant = null;
            StringBuilder corps = new StringBuilder();
            for (XWPFParagraph p : doc.getParagraphs()) {
                String texte = p.getText() == null ? "" : p.getText().trim();
                if (texte.isEmpty()) {
                    continue;
                }
                if (estTitre(p, texte)) {
                    ajouter(sections, titreCourant, corps);
                    titreCourant = texte;
                    corps.setLength(0);
                } else {
                    if (corps.length() > 0) {
                        corps.append('\n');
                    }
                    corps.append(texte);
                }
            }
            ajouter(sections, titreCourant, corps);
        } catch (Exception e) {
            throw new IllegalArgumentException("Document Word illisible ou invalide (.docx attendu).", e);
        }
        return sections;
    }

    private void ajouter(List<SectionWord> sections, String titre, StringBuilder corps) {
        String c = corps.toString().trim();
        if (titre == null && c.isEmpty()) {
            return;
        }
        sections.add(new SectionWord(titre, c));
    }

    /** Un paragraphe est un titre s'il a un style Heading/Titre, ou s'il est court et tout en gras. */
    private boolean estTitre(XWPFParagraph p, String texte) {
        String style = p.getStyle();
        if (style != null) {
            String s = style.toLowerCase(Locale.ROOT);
            if (s.contains("heading") || s.contains("titre") || s.contains("title")) {
                return true;
            }
        }
        List<XWPFRun> runs = p.getRuns();
        if (runs.isEmpty() || texte.length() > 90) {
            return false;
        }
        for (XWPFRun r : runs) {
            if (r.text() != null && !r.text().isBlank() && !r.isBold()) {
                return false;
            }
        }
        return true;
    }
}
