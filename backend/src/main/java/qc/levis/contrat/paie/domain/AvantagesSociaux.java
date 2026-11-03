package qc.levis.contrat.paie.domain;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Les avantages sociaux (assurance maladie, REER collectif, transport…) — port de
 * {@code payroll/benefits.ts} : la cotisation de l'employé est une retenue sur le net, celle de
 * l'employeur une charge et, si imposable, un avantage (case J du Relevé 1). Convertis en éléments
 * de déduction pour que le moteur applique leur traitement fiscal (assurance maladie/dentaire/
 * vision imposable au Québec seulement ; assurance vie, REER, transport, mieux-être aux deux).
 */
public final class AvantagesSociaux {

    private AvantagesSociaux() {
    }

    public static final List<String> TYPES = List.of("health_insurance", "dental", "vision", "life_insurance", "rrsp_match", "transit", "wellness");
    private static final Set<String> IMPOSABLES_FEDERAL = Set.of("life_insurance", "rrsp_match", "transit", "wellness");

    public record Avantage(String id, String employeeId, String kind, double employeeMonthly, double employerMonthly, boolean taxableToEmployee, String startDate, String endDate, String tenantId) {
        public boolean enVigueurLe(String payDate) {
            return startDate.compareTo(payDate) <= 0 && (endDate == null || endDate.compareTo(payDate) >= 0);
        }

        /** L'avantage, en élément de déduction résolu pour la période ({@code benefitsAsDeductionItems}). */
        public MoteurPaie.ElementResolu element(int periodesParAn) {
            double employe = MoteurPaie.arrondir(employeeMonthly * 12 / periodesParAn);
            double employeur = MoteurPaie.arrondir(employerMonthly * 12 / periodesParAn);
            boolean fed = taxableToEmployee && IMPOSABLES_FEDERAL.contains(kind);
            boolean reer = "rrsp_match".equals(kind);
            return new MoteurPaie.ElementResolu(kind, kind, employe, employeur, reer, reer, fed, taxableToEmployee);
        }
    }

    /** Les erreurs d'un nouvel avantage ({@code POST /benefits}), toutes dites ensemble. */
    public static List<String> valider(Map<String, Object> c, boolean employeConnu) {
        List<String> erreurs = new ArrayList<>();
        if (!employeConnu) {
            erreurs.add("valid employeeId required");
        }
        if (!(c.get("kind") instanceof String k) || !TYPES.contains(k)) {
            erreurs.add("kind must be one of " + String.join(",", TYPES));
        }
        if (!(c.get("employeeMonthly") instanceof Number n) || n.doubleValue() < 0) {
            erreurs.add("employeeMonthly >= 0 required");
        }
        if (!(c.get("employerMonthly") instanceof Number n) || n.doubleValue() < 0) {
            erreurs.add("employerMonthly >= 0 required");
        }
        if (c.get("startDate") == null || String.valueOf(c.get("startDate")).isEmpty()) {
            erreurs.add("startDate required");
        }
        return erreurs;
    }

    public static Avantage nouveau(String id, String tenantId, Map<String, Object> c) {
        return new Avantage(id, (String) c.get("employeeId"), (String) c.get("kind"), ((Number) c.get("employeeMonthly")).doubleValue(), ((Number) c.get("employerMonthly")).doubleValue(),
                Boolean.TRUE.equals(c.get("taxableToEmployee")), String.valueOf(c.get("startDate")), c.get("endDate") == null ? null : String.valueOf(c.get("endDate")), tenantId);
    }

    public static List<MoteurPaie.ElementResolu> elements(List<Avantage> avantages, String frequence, String payDate) {
        int periodes = MoteurPaie.periodesParAn(frequence);
        return avantages.stream().filter(a -> a.enVigueurLe(payDate)).map(a -> a.element(periodes)).toList();
    }

    /** Port : les avantages, tous ou d'un employé. */
    public interface Depot {
        List<Avantage> lister(String employeeId);

        java.util.Optional<Avantage> lire(String id);

        void ajouter(Avantage a);

        boolean supprimer(String id);
    }
}
