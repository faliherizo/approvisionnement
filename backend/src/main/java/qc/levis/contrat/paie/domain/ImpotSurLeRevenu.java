package qc.levis.contrat.paie.domain;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * L'impôt sur le revenu d'entreprise — Québec et Canada — au modèle des « États financiers »
 * d'une société par actions : produits − charges déductibles = bénéfice avant impôts, imposé au
 * fédéral (9 %, déduction accordée aux petites entreprises) et au Québec (11,5 %). Les annexes :
 * repas à 50 % (LIR 67.1), bureau à domicile au prorata d'usage, allocation kilométrique, DPA
 * dégressive par catégorie. Un travailleur autonome ou un salarié paie l'impôt des particuliers
 * (barèmes progressifs, crédit personnel de base, abattement du Québec) sur le même revenu net.
 *
 * Portage de {@code payroll/incomeTax.ts}. Les entrées sont lues comme le Node les lit :
 * {@code Number(x) || 0}, une valeur illisible vaut zéro.
 */
public final class ImpotSurLeRevenu {

    private ImpotSurLeRevenu() {
    }

    public static final List<String> PROFILS = List.of("incorporated", "self_employed", "employee");
    public static final double TAUX_FEDERAL_SOCIETE = 0.09;
    public static final double TAUX_QUEBEC_SOCIETE = 0.115;
    static final double MONTANT_PERSONNEL_FEDERAL = 16_129;
    static final double MONTANT_PERSONNEL_QUEBEC = 18_056;
    static final double ABATTEMENT_QUEBEC = 0.165;

    public record Charges(double direct, double subcontracting, double salaries, double recordedExpenses, double mealsDeductible, double homeOfficeDeductible, double kmDeductible, double ccaTotal, double total) {
    }

    public record ActifCourant(double cash, double accountsReceivable, double total) {
    }

    public record Immobilisations(double cost, double accumulatedDepreciation, double net) {
    }

    public record PassifCourant(double accountsPayable, double incomeTaxPayable, double total) {
    }

    public record CapitauxPropres(double shareCapital, double openingRetainedEarnings, double netIncome, double total) {
    }

    public record Bilan(ActifCourant currentAssets, Immobilisations fixedAssets, double totalAssets, PassifCourant currentLiabilities, CapitauxPropres equity, double totalLiabilitiesAndEquity, double balanceCheck, boolean balanced) {
    }

    public record TauxAppliques(Double federal, Double quebec) {
    }

    public record Resultat(String profile, double totalRevenue, Charges charges, double taxableIncome, double federalTax, double quebecTax, double totalTax, double netIncome, TauxAppliques ratesApplied, Bilan balanceSheet) {
    }

    public static double r2(double n) {
        return Math.round(n * 100) / 100.0;
    }

    /** {@code Number(x) || 0} : un nombre, une chaîne numérique, sinon zéro. */
    public static double nombre(Object v) {
        if (v instanceof Number n) {
            return Double.isFinite(n.doubleValue()) ? n.doubleValue() : 0;
        }
        if (v instanceof String s && !s.isBlank()) {
            try {
                return Double.parseDouble(s.trim());
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    private static Map<?, ?> carte(Object v) {
        return v instanceof Map<?, ?> m ? m : Map.of();
    }

    private static List<?> liste(Object v) {
        return v instanceof List<?> l ? l : List.of();
    }

    private static double sommeDes(Object lignes, String cle) {
        return liste(lignes).stream().mapToDouble(l -> nombre(carte(l).get(cle))).sum();
    }

    static double impotProgressif(double imposable, List<Baremes.Tranche> tranches) {
        if (imposable <= 0) {
            return 0;
        }
        double impot = 0;
        double precedent = 0;
        for (Baremes.Tranche t : tranches) {
            double plafond = t.upTo() == null ? Double.POSITIVE_INFINITY : t.upTo();
            if (imposable > plafond) {
                impot += (plafond - precedent) * t.rate();
                precedent = plafond;
            } else {
                return impot + (imposable - precedent) * t.rate();
            }
        }
        return impot;
    }

    /** L'impôt des particuliers d'un résident du Québec : barèmes, crédit personnel de base, abattement fédéral. */
    static double[] impotDesParticuliers(double imposable) {
        double federalBrut = impotProgressif(imposable, Baremes.TRANCHES_FEDERALES);
        double federal = r2(Math.max(0, federalBrut - MONTANT_PERSONNEL_FEDERAL * Baremes.TRANCHES_FEDERALES.get(0).rate()) * (1 - ABATTEMENT_QUEBEC));
        double quebecBrut = impotProgressif(imposable, Baremes.TRANCHES_QUEBEC);
        double quebec = r2(Math.max(0, quebecBrut - MONTANT_PERSONNEL_QUEBEC * Baremes.TRANCHES_QUEBEC.get(0).rate()));
        return new double[]{federal, quebec};
    }

    static Charges charges(Map<String, Object> e) {
        double direct = sommeDes(e.get("directExpenses"), "amount");
        double sousTraitance = nombre(e.get("subcontracting"));
        double salaires = nombre(e.get("salaries"));
        double enregistrees = nombre(e.get("recordedExpensesDeductible"));
        Map<?, ?> repas = carte(e.get("meals"));
        double repasDeductibles = (nombre(repas.get("regular")) + nombre(repas.get("events"))) * 0.5;
        double bureau = e.get("homeOffice") instanceof Map<?, ?> ho ? sommeDes(ho.get("items"), "amount") * nombre(ho.get("businessUsePct")) : 0;
        Map<?, ?> km = carte(e.get("km"));
        double kilometrage = e.get("km") instanceof Map ? nombre(km.get("distanceKm")) * nombre(km.get("ratePerKm")) : 0;
        double dpa = liste(e.get("cca")).stream().mapToDouble(c -> {
            Map<?, ?> m = carte(c);
            double base = nombre(m.get("openingUcc")) + nombre(m.get("additions")) - nombre(m.get("dispositions"));
            return Math.max(0, base) * nombre(m.get("rate"));
        }).sum();
        double total = direct + sousTraitance + salaires + enregistrees + repasDeductibles + bureau + kilometrage + dpa;
        return new Charges(r2(direct), r2(sousTraitance), r2(salaires), r2(enregistrees), r2(repasDeductibles), r2(bureau), r2(kilometrage), r2(dpa), r2(total));
    }

    static Bilan bilan(Map<String, Object> e, Charges charges, double impotAPayer, double beneficeNet) {
        double encaisse = nombre(e.get("cash"));
        double clients = nombre(e.get("accountsReceivable"));
        double coutImmobilisations = liste(e.get("cca")).stream().mapToDouble(c -> nombre(carte(c).get("openingUcc")) + nombre(carte(c).get("additions"))).sum();
        double net = coutImmobilisations - charges.ccaTotal();
        double totalActif = encaisse + clients + net;
        double fournisseurs = nombre(e.get("accountsPayable"));
        double passif = fournisseurs + impotAPayer;
        // Le capital-actions vaut 100 par défaut pour une société par actions, rien pour un particulier.
        double capitalActions = e.get("shareCapital") != null ? nombre(e.get("shareCapital")) : "incorporated".equals(e.get("profile")) ? 100 : 0;
        double reportes = nombre(e.get("openingRetainedEarnings"));
        double capitaux = capitalActions + reportes + beneficeNet;
        double total = passif + capitaux;
        double ecart = r2(totalActif - total);
        return new Bilan(new ActifCourant(r2(encaisse), r2(clients), r2(encaisse + clients)), new Immobilisations(r2(coutImmobilisations), r2(charges.ccaTotal()), r2(net)), r2(totalActif),
                new PassifCourant(r2(fournisseurs), r2(impotAPayer), r2(passif)), new CapitauxPropres(r2(capitalActions), r2(reportes), r2(beneficeNet), r2(capitaux)), r2(total), ecart, Math.abs(ecart) < 0.01);
    }

    /** {@code computeIncomeTax} : le profil décide du régime — taux des sociétés, ou barèmes des particuliers. */
    public static Resultat calculer(Map<String, Object> e) {
        String profil = String.valueOf(e.get("profile"));
        double produits = r2(nombre(e.get("revenue")) + nombre(e.get("otherIncome")));
        Charges charges = charges(e);
        double imposable = r2(produits - charges.total());
        double federal;
        double quebec;
        TauxAppliques taux;
        if ("incorporated".equals(profil)) {
            double tf = e.get("federalRate") == null ? TAUX_FEDERAL_SOCIETE : nombre(e.get("federalRate"));
            double tq = e.get("quebecRate") == null ? TAUX_QUEBEC_SOCIETE : nombre(e.get("quebecRate"));
            double base = Math.max(0, imposable);
            federal = r2(base * tf);
            quebec = r2(base * tq);
            taux = new TauxAppliques(tf, tq);
        } else {
            double[] p = impotDesParticuliers(Math.max(0, imposable));
            federal = p[0];
            quebec = p[1];
            taux = new TauxAppliques(null, null);
        }
        double totalImpot = r2(federal + quebec);
        double beneficeNet = r2(imposable - totalImpot);
        return new Resultat(profil, produits, charges, imposable, federal, quebec, totalImpot, beneficeNet, taux, bilan(e, charges, totalImpot, beneficeNet));
    }

    /** {@code POST /income-tax/preview} : le profil et un chiffre d'affaires numérique positif. */
    public static void validerApercu(Map<String, Object> e) {
        if (!(e.get("profile") instanceof String p) || !PROFILS.contains(p)) {
            throw new IllegalArgumentException("profile must be incorporated | self_employed | employee");
        }
        if (!(e.get("revenue") instanceof Number n) || n.doubleValue() < 0) {
            throw new IllegalArgumentException("revenue must be a number >= 0");
        }
    }

    /** Le résultat comme le Node le sérialise, pour la persistance et les exports. */
    public static Map<String, Object> vue(Resultat r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("profile", r.profile());
        m.put("totalRevenue", r.totalRevenue());
        Map<String, Object> c = new LinkedHashMap<>();
        c.put("direct", r.charges().direct());
        c.put("subcontracting", r.charges().subcontracting());
        c.put("salaries", r.charges().salaries());
        c.put("recordedExpenses", r.charges().recordedExpenses());
        c.put("mealsDeductible", r.charges().mealsDeductible());
        c.put("homeOfficeDeductible", r.charges().homeOfficeDeductible());
        c.put("kmDeductible", r.charges().kmDeductible());
        c.put("ccaTotal", r.charges().ccaTotal());
        c.put("total", r.charges().total());
        m.put("charges", c);
        m.put("taxableIncome", r.taxableIncome());
        m.put("federalTax", r.federalTax());
        m.put("quebecTax", r.quebecTax());
        m.put("totalTax", r.totalTax());
        m.put("netIncome", r.netIncome());
        Map<String, Object> taux = new LinkedHashMap<>();
        taux.put("federal", r.ratesApplied().federal());
        taux.put("quebec", r.ratesApplied().quebec());
        m.put("ratesApplied", taux);
        Bilan b = r.balanceSheet();
        Map<String, Object> bilan = new LinkedHashMap<>();
        bilan.put("currentAssets", Map.of("cash", b.currentAssets().cash(), "accountsReceivable", b.currentAssets().accountsReceivable(), "total", b.currentAssets().total()));
        bilan.put("fixedAssets", Map.of("cost", b.fixedAssets().cost(), "accumulatedDepreciation", b.fixedAssets().accumulatedDepreciation(), "net", b.fixedAssets().net()));
        bilan.put("totalAssets", b.totalAssets());
        bilan.put("currentLiabilities", Map.of("accountsPayable", b.currentLiabilities().accountsPayable(), "incomeTaxPayable", b.currentLiabilities().incomeTaxPayable(), "total", b.currentLiabilities().total()));
        bilan.put("equity", Map.of("shareCapital", b.equity().shareCapital(), "openingRetainedEarnings", b.equity().openingRetainedEarnings(), "netIncome", b.equity().netIncome(), "total", b.equity().total()));
        bilan.put("totalLiabilitiesAndEquity", b.totalLiabilitiesAndEquity());
        bilan.put("balanceCheck", b.balanceCheck());
        bilan.put("balanced", b.balanced());
        m.put("balanceSheet", bilan);
        return m;
    }
}
