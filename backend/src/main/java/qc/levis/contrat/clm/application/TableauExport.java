package qc.levis.contrat.clm.application;

import java.util.List;

/**
 * Tableau d'un rapport ou du relevé d'utilisation, tel qu'affiché et exporté (Annexe 10, AF-37). Le CSV, séparé par « ; »
 * pour les tableurs en français, neutralise les cellules qui commencent par un caractère de formule.
 */
public record TableauExport(String titre, List<String> colonnes, List<List<String>> lignes) {

    public String csv() {
        StringBuilder sb = new StringBuilder(String.join(";", colonnes.stream().map(TableauExport::cellule).toList())).append('\n');
        for (List<String> l : lignes) {
            sb.append(String.join(";", l.stream().map(TableauExport::cellule).toList())).append('\n');
        }
        return sb.toString();
    }

    static String cellule(String v) {
        String s = v == null ? "" : v;
        if (!s.isEmpty() && "=+-@".indexOf(s.charAt(0)) >= 0) {
            s = "'" + s;
        }
        return s.contains(";") || s.contains("\"") || s.contains("\n") ? "\"" + s.replace("\"", "\"\"") + "\"" : s;
    }
}
