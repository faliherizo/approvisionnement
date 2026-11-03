package qc.levis.contrat.clm.domain;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Option CLM/IA (ST-15, UI-24) : extraction de métadonnées, classification et détection d'anomalies sur le texte
 * d'un contrat. Chaque résultat porte un niveau de confiance et reste « à réviser » jusqu'à validation humaine.
 */
public final class AnalyseContrat {

    private static final Pattern MONTANT = Pattern.compile("(\\d{1,3}(?:[ \\u00A0\\u202F]\\d{3})+|\\d+)(?:[,.](\\d{2}))?\\s?\\$");
    private static final Pattern DATE = Pattern.compile("\\b(\\d{4}-\\d{2}-\\d{2})\\b");
    private static final Pattern DUREE = Pattern.compile("\\b(\\d{1,3})\\s+(mois|ans?|années?)\\b");
    private static final Map<String, List<String>> CATEGORIES = new LinkedHashMap<>();
    private static final Map<String, List<String>> CLAUSES_ATTENDUES = new LinkedHashMap<>();

    static {
        CATEGORIES.put("SUBVENTION", List.of("subvention", "aide financiere", "programme", "beneficiaire"));
        CATEGORIES.put("SERVICES", List.of("prestataire", "services professionnels", "honoraires", "mandat"));
        CATEGORIES.put("CONFIDENTIALITE", List.of("confidentialite", "non-divulgation", "renseignements confidentiels"));
        CATEGORIES.put("PRET", List.of("pret", "remboursement", "taux d'interet", "emprunteur"));
        CLAUSES_ATTENDUES.put("RESILIATION", List.of("resiliation", "resilier"));
        CLAUSES_ATTENDUES.put("CONFIDENTIALITE", List.of("confidentialite", "confidentiel"));
        CLAUSES_ATTENDUES.put("RENSEIGNEMENTS_PERSONNELS", List.of("renseignements personnels", "loi 25"));
        CLAUSES_ATTENDUES.put("DROIT_APPLICABLE", List.of("lois du quebec", "droit applicable", "tribunaux du quebec"));
    }

    public record Element(String cle, String valeur, double confiance) {
    }

    public record Resultat(List<Element> metadonnees, String categorie, double confianceCategorie, List<Element> anomalies) {
    }

    private AnalyseContrat() {
    }

    public static Resultat analyser(String texte, BigDecimal montantAttendu) {
        String brut = texte == null ? "" : texte;
        String norme = normaliser(brut);
        List<Element> meta = new ArrayList<>();
        List<BigDecimal> montants = montants(brut);
        montants.stream().max(BigDecimal::compareTo).ifPresent(m -> meta.add(new Element("montant", m.toPlainString(), 0.8)));
        Set<String> dates = new LinkedHashSet<>();
        Matcher d = DATE.matcher(brut);
        while (d.find()) {
            dates.add(d.group(1));
        }
        dates.stream().findFirst().ifPresent(v -> meta.add(new Element("dateDebut", v, 0.6)));
        Matcher du = DUREE.matcher(norme);
        if (du.find()) {
            meta.add(new Element("duree", du.group(1) + " " + du.group(2), 0.7));
        }
        String categorie = "INDETERMINEE";
        double confiance = 0;
        for (Map.Entry<String, List<String>> e : CATEGORIES.entrySet()) {
            long trouves = e.getValue().stream().filter(norme::contains).count();
            double score = (double) trouves / e.getValue().size();
            if (score > confiance) {
                confiance = score;
                categorie = e.getKey();
            }
        }
        List<Element> anomalies = new ArrayList<>();
        for (Map.Entry<String, List<String>> e : CLAUSES_ATTENDUES.entrySet()) {
            if (e.getValue().stream().noneMatch(norme::contains)) {
                anomalies.add(new Element("clauseAbsente", e.getKey(), 0.7));
            }
        }
        if (montantAttendu != null && !montants.isEmpty() && montants.stream().noneMatch(m -> m.compareTo(montantAttendu) == 0)) {
            anomalies.add(new Element("montantDivergent", "attendu " + montantAttendu.toPlainString(), 0.75));
        }
        return new Resultat(meta, categorie, Math.round(confiance * 100) / 100.0, anomalies);
    }

    static List<BigDecimal> montants(String texte) {
        List<BigDecimal> res = new ArrayList<>();
        Matcher m = MONTANT.matcher(texte);
        while (m.find()) {
            String entier = m.group(1).replaceAll("[ \\u00A0\\u202F]", "");
            res.add(new BigDecimal(m.group(2) == null ? entier : entier + "." + m.group(2)));
        }
        return res;
    }

    private static String normaliser(String s) {
        return Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }
}
