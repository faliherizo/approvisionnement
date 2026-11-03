package qc.levis.contrat.paie.domain;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Barèmes de paie 2025 (Québec) et référentiels fixes. PORTÉ depuis Nexu VINA
 * (`ca.vina.nexu.paie.domaine.Baremes`) — classe pure, aucune dépendance framework, valeurs identiques
 * (RRQ/RRQ2, AE, RQAP, CNESST, FSS, tranches fédérales/Québec). Versionnage annuel à prévoir.
 */
public final class Baremes {

    private Baremes() {
    }

    public record Tranche(Double upTo, double rate) {
        public Map<String, Object> vue() {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("upTo", upTo);
            m.put("rate", rate);
            return m;
        }
    }

    public static final List<Tranche> TRANCHES_FEDERALES = List.of(new Tranche(57_375.0, 0.15), new Tranche(114_750.0, 0.205), new Tranche(177_882.0, 0.26), new Tranche(253_414.0, 0.29), new Tranche(null, 0.33));
    public static final List<Tranche> TRANCHES_QUEBEC = List.of(new Tranche(53_255.0, 0.14), new Tranche(106_495.0, 0.19), new Tranche(129_590.0, 0.24), new Tranche(null, 0.2575));

    /** Les taux publiés par {@code GET /api/paie/taux}. */
    public static Map<String, Object> taux2025() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("year", 2025);
        m.put("federalBrackets", TRANCHES_FEDERALES.stream().map(Tranche::vue).toList());
        m.put("federalBasicPersonalAmount", 16_129);
        m.put("quebecAbatementRate", 0.165);
        m.put("qcBrackets", TRANCHES_QUEBEC.stream().map(Tranche::vue).toList());
        m.put("qcBasicPersonalAmount", 18_056);
        m.put("qppRate", 0.0640);
        m.put("qppMaxPensionable", 71_300);
        m.put("qppBasicExemption", 3_500);
        m.put("qpp2Rate", 0.04);
        m.put("qpp2MaxPensionable", 81_200);
        m.put("eiRate", 0.0131);
        m.put("eiMaxInsurable", 65_700);
        m.put("eiEmployerMultiplier", 1.4);
        m.put("qpipEmployeeRate", 0.00494);
        m.put("qpipEmployerRate", 0.00692);
        m.put("qpipMaxInsurable", 98_000);
        m.put("cnesstRate", 0.0178);
        m.put("fssRate", 0.0165);
        return m;
    }

    public static final Map<String, Integer> PERIODES_PAR_AN = new LinkedHashMap<>();

    static {
        PERIODES_PAR_AN.put("weekly", 52);
        PERIODES_PAR_AN.put("biweekly", 26);
        PERIODES_PAR_AN.put("semimonthly", 24);
        PERIODES_PAR_AN.put("monthly", 12);
    }

    /** {@code [{value, periodsPerYear}]}. */
    public static List<Map<String, Object>> frequences() {
        return PERIODES_PAR_AN.entrySet().stream().map(e -> Map.<String, Object>of("value", e.getKey(), "periodsPerYear", e.getValue())).toList();
    }
}
