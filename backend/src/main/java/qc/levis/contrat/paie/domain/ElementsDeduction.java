package qc.levis.contrat.paie.domain;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Les éléments de paie récurrents (REER/RPA, RVER/RPAC, assurance collective, cotisation
 * syndicale, saisie-arrêt…) — port de {@code payroll/deductionItems.ts}. Chaque type porte son
 * traitement fiscal par défaut (table de vérité de Revenu Québec) ; la résolution pour une
 * période applique le pourcentage du brut, le plafond annuel au prorata et la part employé.
 */
public final class ElementsDeduction {

    private ElementsDeduction() {
    }

    /** Le traitement fiscal par défaut d'un type ({@code DEFAULT_TAX_TREATMENT}). */
    public record Traitement(String paidBy, boolean reducesFederalTax, boolean reducesQuebecTax, boolean taxableFederal, boolean taxableQuebec) {
    }

    public static final Map<String, Traitement> TRAITEMENTS = new LinkedHashMap<>();

    static {
        Traitement reer = new Traitement("employee", true, true, false, false);
        TRAITEMENTS.put("rrsp", reer);
        TRAITEMENTS.put("rpp", reer);
        TRAITEMENTS.put("rver", reer);
        TRAITEMENTS.put("rpac", reer);
        // Cotisation syndicale : réduit le fédéral, pas le Québec (asymétrie RQ).
        TRAITEMENTS.put("union_dues", new Traitement("employee", true, false, false, false));
        // Assurance maladie/dentaire collective payée par l'employeur : avantage imposable QC seulement.
        TRAITEMENTS.put("group_insurance_health", new Traitement("employer", false, false, false, true));
        TRAITEMENTS.put("group_insurance_dental", new Traitement("employer", false, false, false, true));
        // Assurance vie payée par l'employeur : avantage imposable fédéral et Québec.
        TRAITEMENTS.put("group_insurance_life", new Traitement("employer", false, false, true, true));
        TRAITEMENTS.put("garnishment", new Traitement("employee", false, false, false, false));
        TRAITEMENTS.put("other", new Traitement("employee", false, false, false, false));
    }

    public static final List<String> TYPES = List.copyOf(TRAITEMENTS.keySet());

    public record Element(String id, String tenantId, String employeeId, String kind, String basis, double amount, String paidBy, Double employeeShare,
                          boolean reducesFederalTax, boolean reducesQuebecTax, boolean taxableFederal, boolean taxableQuebec, Double annualCap, String label, boolean active,
                          String startDate, String endDate, String createdAt, String updatedAt) {
        /** L'élément vaut pour la date de paie s'il est actif et que la date tombe dans sa plage. */
        public boolean valableLe(String payDate) {
            if (!active) {
                return false;
            }
            if (payDate != null && startDate != null && payDate.compareTo(startDate) < 0) {
                return false;
            }
            return payDate == null || endDate == null || payDate.compareTo(endDate) <= 0;
        }

        /** Résolu pour une période ({@code resolveDeductionItemsForPeriod}) : montant, plafond prorata, part employé. */
        public MoteurPaie.ElementResolu resoudre(double brutPeriode, int periodesParAn) {
            double montant = "percent".equals(basis) ? MoteurPaie.arrondir(amount / 100 * brutPeriode) : MoteurPaie.arrondir(amount);
            if (annualCap != null && annualCap > 0) {
                montant = Math.min(montant, MoteurPaie.arrondir(annualCap / periodesParAn));
            }
            double part = switch (paidBy) {
                case "shared" -> Math.min(1, Math.max(0, employeeShare == null ? 0.5 : employeeShare));
                case "employee" -> 1;
                default -> 0;
            };
            return new MoteurPaie.ElementResolu(kind, label, MoteurPaie.arrondir(montant * part), MoteurPaie.arrondir(montant * (1 - part)), reducesFederalTax, reducesQuebecTax, taxableFederal, taxableQuebec);
        }
    }

    private static Double nombre(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof Number n) {
            return n.doubleValue();
        }
        try {
            return Double.parseDouble(String.valueOf(v));
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }

    private static boolean bool(Object v, boolean defaut) {
        return v == null ? defaut : Boolean.TRUE.equals(v);
    }

    private static String texte(Object v) {
        return v == null ? null : String.valueOf(v);
    }

    /** Un nouvel élément depuis le corps de la requête ({@code createDeductionItem}) : le type et le montant sont vérifiés, le reste prend le défaut du type. */
    public static Element nouveau(String id, String tenantId, String employeeId, Map<String, Object> c, String instant) {
        String kind = texte(c.get("kind"));
        if (kind == null || !TRAITEMENTS.containsKey(kind)) {
            throw new IllegalArgumentException("invalid kind");
        }
        Double montant = nombre(c.get("amount"));
        if (montant == null || !(montant >= 0)) {
            throw new IllegalArgumentException("amount must be >= 0");
        }
        String basis = "percent".equals(c.get("basis")) ? "percent" : "fixed";
        if (basis.equals("percent") && montant > 100) {
            throw new IllegalArgumentException("percent amount must be 0–100");
        }
        Traitement d = TRAITEMENTS.get(kind);
        return new Element(id, tenantId, employeeId, kind, basis, montant, c.get("paidBy") == null ? d.paidBy() : texte(c.get("paidBy")), nombre(c.get("employeeShare")),
                bool(c.get("reducesFederalTax"), d.reducesFederalTax()), bool(c.get("reducesQuebecTax"), d.reducesQuebecTax()), bool(c.get("taxableFederal"), d.taxableFederal()),
                bool(c.get("taxableQuebec"), d.taxableQuebec()), nombre(c.get("annualCap")), texte(c.get("label")), bool(c.get("active"), true), texte(c.get("startDate")), texte(c.get("endDate")), instant, instant);
    }

    /** Le correctif ({@code updateDeductionItem}) : une clé absente garde sa valeur, une clé présente à {@code null} efface. */
    public static Element corriger(Element cur, Map<String, Object> p, String instant) {
        double montant = p.get("amount") == null ? cur.amount() : nombre(p.get("amount"));
        if (montant < 0 || Double.isNaN(montant)) {
            throw new IllegalArgumentException("amount must be >= 0");
        }
        return new Element(cur.id(), cur.tenantId(), cur.employeeId(), p.get("kind") == null ? cur.kind() : texte(p.get("kind")), p.get("basis") == null ? cur.basis() : texte(p.get("basis")), montant,
                p.get("paidBy") == null ? cur.paidBy() : texte(p.get("paidBy")), p.containsKey("employeeShare") ? nombre(p.get("employeeShare")) : cur.employeeShare(),
                bool(p.get("reducesFederalTax"), cur.reducesFederalTax()), bool(p.get("reducesQuebecTax"), cur.reducesQuebecTax()), bool(p.get("taxableFederal"), cur.taxableFederal()),
                bool(p.get("taxableQuebec"), cur.taxableQuebec()), p.containsKey("annualCap") ? nombre(p.get("annualCap")) : cur.annualCap(), p.containsKey("label") ? texte(p.get("label")) : cur.label(),
                bool(p.get("active"), cur.active()), p.containsKey("startDate") ? texte(p.get("startDate")) : cur.startDate(), p.containsKey("endDate") ? texte(p.get("endDate")) : cur.endDate(), cur.createdAt(), instant);
    }

    public static List<MoteurPaie.ElementResolu> resoudre(List<Element> elements, double brutPeriode, int periodesParAn, String payDate) {
        List<MoteurPaie.ElementResolu> r = new ArrayList<>();
        for (Element e : elements) {
            if (e.valableLe(payDate)) {
                r.add(e.resoudre(brutPeriode, periodesParAn));
            }
        }
        return r;
    }

    /** Port : les éléments d'un employé, cloisonnés par établissement. */
    public interface Depot {
        List<Element> lister(String tenantId, String employeeId, boolean actifsSeulement);

        java.util.Optional<Element> lire(String tenantId, String id);

        void ajouter(Element e);

        void remplacer(Element e);

        boolean supprimer(String tenantId, String id);
    }
}
