package qc.levis.contrat.regle.application;

import java.math.BigDecimal;
import java.util.*;
import qc.levis.contrat.regle.domain.ConditionDef;
import qc.levis.contrat.regle.domain.RegleDef;

/**
 * Moteur de règles PUR (sans E/S) : évalue des règles configurables contre un jeu de réponses.
 * Types : VISIBILITE (masque la cible si les conditions ne sont pas satisfaites), OBLIGATION,
 * VALIDATION (message si conditions satisfaites), CALCUL (expression « op:champ1,champ2 » avec
 * détection de cycle), ROUTAGE (rôle si conditions satisfaites). Testable isolément.
 */
public final class MoteurRegles {

    private MoteurRegles() {}

    public static ResultatEvaluation evaluer(List<RegleDef> regles, Map<String, Object> valeurs) {
        List<String> masques = new ArrayList<>();
        List<String> obligatoires = new ArrayList<>();
        List<String> erreurs = new ArrayList<>();
        List<String> routage = new ArrayList<>();
        Map<String, BigDecimal> calculs = new LinkedHashMap<>();
        if (regles == null) regles = List.of();

        // index des calculs pour résolution avec détection de cycle
        Map<String, String> expressions = new HashMap<>();
        for (RegleDef r : regles) {
            if ("CALCUL".equalsIgnoreCase(r.type()) && r.cible() != null) expressions.put(r.cible(), r.valeur());
        }
        for (String cible : expressions.keySet()) {
            resoudreCalcul(cible, expressions, valeurs, calculs, new LinkedHashSet<>());
        }

        for (RegleDef r : regles) {
            if (r.type() == null) continue;
            boolean ok = conditionsSatisfaites(r, valeurs);
            switch (r.type().toUpperCase()) {
                case "VISIBILITE" -> { if (!ok && r.cible() != null) masques.add(r.cible()); }
                case "OBLIGATION" -> { if (ok && r.cible() != null) obligatoires.add(r.cible()); }
                case "VALIDATION" -> { if (ok) erreurs.add(r.valeur() != null ? r.valeur() : "Validation échouée"); }
                case "ROUTAGE" -> { if (ok && r.cible() != null) routage.add(r.cible()); }
                default -> { /* CALCUL traité ci-dessus */ }
            }
        }
        return new ResultatEvaluation(masques, obligatoires, erreurs, calculs, routage);
    }

    static boolean conditionsSatisfaites(RegleDef r, Map<String, Object> valeurs) {
        List<ConditionDef> conds = r.conditions();
        if (conds == null || conds.isEmpty()) return true;
        boolean ou = "OU".equalsIgnoreCase(r.combinateur());
        boolean acc = !ou;
        for (ConditionDef c : conds) {
            boolean v = evaluerCondition(c, valeurs);
            acc = ou ? (acc || v) : (acc && v);
        }
        return acc;
    }

    static boolean evaluerCondition(ConditionDef c, Map<String, Object> valeurs) {
        Object brut = valeurs.get(c.champ());
        String val = brut == null ? null : String.valueOf(brut);
        String attendu = c.valeur();
        String op = c.operateur() == null ? "EGAL" : c.operateur().toUpperCase();
        switch (op) {
            case "VIDE": return val == null || val.isBlank();
            case "NON_VIDE": return val != null && !val.isBlank();
            case "EGAL": return Objects.equals(val, attendu);
            case "DIFFERENT": return !Objects.equals(val, attendu);
            case "CONTIENT": return val != null && attendu != null && val.contains(attendu);
            case "SUP": case "INF": case "SUPEGAL": case "INFEGAL": {
                BigDecimal a = nombre(val), b = nombre(attendu);
                if (a == null || b == null) return false;
                int cmp = a.compareTo(b);
                return switch (op) {
                    case "SUP" -> cmp > 0;
                    case "INF" -> cmp < 0;
                    case "SUPEGAL" -> cmp >= 0;
                    default -> cmp <= 0;
                };
            }
            default: return false;
        }
    }

    private static BigDecimal resoudreCalcul(String cible, Map<String, String> expr, Map<String, Object> valeurs,
                                             Map<String, BigDecimal> calculs, LinkedHashSet<String> enCours) {
        if (calculs.containsKey(cible)) return calculs.get(cible);
        if (!enCours.add(cible)) return BigDecimal.ZERO; // cycle détecté → neutralise
        String expression = expr.get(cible);
        BigDecimal res = BigDecimal.ZERO;
        if (expression != null && expression.contains(":")) {
            String[] parts = expression.split(":", 2);
            String op = parts[0].trim().toLowerCase();
            String[] operandes = parts[1].split(",");
            List<BigDecimal> vals = new ArrayList<>();
            for (String o : operandes) {
                String code = o.trim();
                BigDecimal v = expr.containsKey(code)
                        ? resoudreCalcul(code, expr, valeurs, calculs, enCours)
                        : nombre(valeurs.get(code) == null ? null : String.valueOf(valeurs.get(code)));
                vals.add(v == null ? BigDecimal.ZERO : v);
            }
            res = appliquer(op, vals);
        }
        enCours.remove(cible);
        calculs.put(cible, res);
        return res;
    }

    private static BigDecimal appliquer(String op, List<BigDecimal> vals) {
        if (vals.isEmpty()) return BigDecimal.ZERO;
        BigDecimal acc = vals.get(0);
        for (int i = 1; i < vals.size(); i++) {
            BigDecimal v = vals.get(i);
            switch (op) {
                case "somme" -> acc = acc.add(v);
                case "difference" -> acc = acc.subtract(v);
                case "produit" -> acc = acc.multiply(v);
                case "quotient" -> acc = v.signum() == 0 ? acc : acc.divide(v, 6, java.math.RoundingMode.HALF_UP);
                default -> { /* op inconnu : ignorer */ }
            }
        }
        return acc;
    }

    private static BigDecimal nombre(String s) {
        if (s == null || s.isBlank()) return null;
        try { return new BigDecimal(s.trim().replace(",", ".")); } catch (NumberFormatException e) { return null; }
    }
}
