package qc.levis.contrat.processus.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import qc.levis.contrat.processus.domain.ConditionEtape;
import qc.levis.contrat.processus.domain.EtapeDef;
import qc.levis.contrat.processus.domain.WorkflowDef;

/**
 * Moteur de routage de workflow — PUR (aucune E/S, aucune dépendance framework), donc testable à 100 %.
 * Calcule, pour un jeu de réponses, la séquence d'étapes réellement applicables : une étape s'applique si
 * TOUTES ses conditions sont vraies (routage par montant, type, etc. — écran 5 « Montant > seuil ? »).
 */
public final class MoteurWorkflow {

    private MoteurWorkflow() {}

    /** Étapes applicables dans l'ordre de définition, filtrées par leurs conditions. */
    public static List<EtapeDef> etapesApplicables(WorkflowDef def, Map<String, Object> valeurs) {
        List<EtapeDef> res = new ArrayList<>();
        Map<String, Object> v = valeurs == null ? Map.of() : valeurs;
        for (EtapeDef e : def.etapes()) {
            if (conditionsRemplies(e.conditions(), v)) res.add(e);
        }
        return res;
    }

    static boolean conditionsRemplies(List<ConditionEtape> conditions, Map<String, Object> valeurs) {
        for (ConditionEtape c : conditions) {
            if (!evalue(c, valeurs)) return false;
        }
        return true;
    }

    static boolean evalue(ConditionEtape c, Map<String, Object> valeurs) {
        Object brut = valeurs.get(c.champ());
        String val = brut == null ? null : String.valueOf(brut);
        String ref = c.valeur();
        return switch (c.operateur() == null ? "" : c.operateur().toUpperCase()) {
            case "EGAL" -> java.util.Objects.equals(val, ref);
            case "DIFFERENT" -> !java.util.Objects.equals(val, ref);
            case "CONTIENT" -> val != null && ref != null && val.contains(ref);
            case "VIDE" -> val == null || val.isBlank();
            case "NON_VIDE" -> val != null && !val.isBlank();
            case "SUP" -> compare(val, ref) > 0;
            case "INF" -> compare(val, ref) < 0;
            case "SUPEGAL" -> compare(val, ref) >= 0;
            case "INFEGAL" -> compare(val, ref) <= 0;
            default -> false;
        };
    }

    /** Comparaison numérique ; renvoie 0 (non comparable) si l'une des valeurs n'est pas un nombre. */
    private static int compare(String a, String b) {
        try {
            return Double.compare(Double.parseDouble(a.replace(",", ".")), Double.parseDouble(b.replace(",", ".")));
        } catch (NullPointerException | NumberFormatException e) {
            return 0;
        }
    }
}
