package qc.levis.contrat.notification.application;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Fusion PURE d'un gabarit texte avec des valeurs : remplace {@code {{code}}} par la valeur (ou vide). */
public final class GabaritTexte {

    private static final Pattern MARQUEUR = Pattern.compile("\\{\\{\\s*([\\w.-]+)\\s*}}");

    private GabaritTexte() {}

    public static String fusionner(String gabarit, Map<String, Object> valeurs) {
        if (gabarit == null) return "";
        Map<String, Object> v = valeurs == null ? Map.of() : valeurs;
        Matcher m = MARQUEUR.matcher(gabarit);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            Object val = v.get(m.group(1));
            m.appendReplacement(sb, Matcher.quoteReplacement(val == null ? "" : String.valueOf(val)));
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
