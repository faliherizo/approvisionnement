package qc.levis.contrat.paie.domain;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToDoubleFunction;

/**
 * Calcul des retenues d'une paie — PORTÉ depuis Nexu VINA ({@code ca.vina.nexu.paie.domaine.MoteurPaie}),
 * cœur de calcul pur (annualisation RQ TP-1015.G / ARC T4127). Valeurs identiques à la source. Seule la
 * queue « bulletins émis / absences / ports de persistance » (couplée à d'autres contextes) n'est pas
 * reprise ici : ce fichier ne porte QUE le calcul pur (impôt féd.+QC séparés, RRQ/RRQ2, AE, RQAP, CNESST,
 * FSS, parts employé/employeur, net, cumul YTD), testable à 100 % sans E/S.
 */
public final class MoteurPaie {

    private MoteurPaie() {
    }

    /** Les taux de l'année (cf. {@link Baremes#taux2025()}). */
    public record Taux(List<Baremes.Tranche> tranchesFederales, double montantPersonnelFederal, double abattementQuebec, List<Baremes.Tranche> tranchesQuebec,
                       double montantPersonnelQuebec, double tauxRrq, double maxRrq, double exemptionRrq, double tauxRrq2, double maxRrq2, double tauxAe, double maxAe,
                       double multiplicateurAeEmployeur, double tauxRqapEmploye, double tauxRqapEmployeur, double maxRqap, double tauxCnesst, double tauxFss) {
        public static final Taux DE_2025 = new Taux(Baremes.TRANCHES_FEDERALES, 16_129, 0.165, Baremes.TRANCHES_QUEBEC, 18_056, 0.0640, 71_300, 3_500, 0.04, 81_200,
                0.0131, 65_700, 1.4, 0.00494, 0.00692, 98_000, 0.0178, 0.0165);
    }

    /** Le cumul annuel ({@code YTD}) : ce qui a déjà été cotisé, pour plafonner. */
    public record Ytd(double qppPensionable, double qppContribution, double qpp2Contribution, double eiInsurable, double eiContribution, double qpipInsurable, double qpipContribution) {
        public static final Ytd ZERO = new Ytd(0, 0, 0, 0, 0, 0, 0);

        public static Ytd depuis(Map<String, Object> m) {
            if (m == null) {
                return ZERO;
            }
            return new Ytd(nombre(m.get("qppPensionable")), nombre(m.get("qppContribution")), nombre(m.get("qpp2Contribution")), nombre(m.get("eiInsurable")),
                    nombre(m.get("eiContribution")), nombre(m.get("qpipInsurable")), nombre(m.get("qpipContribution")));
        }

        public Map<String, Object> vue() {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("qppPensionable", qppPensionable);
            m.put("qppContribution", qppContribution);
            m.put("qpp2Contribution", qpp2Contribution);
            m.put("eiInsurable", eiInsurable);
            m.put("eiContribution", eiContribution);
            m.put("qpipInsurable", qpipInsurable);
            m.put("qpipContribution", qpipContribution);
            return m;
        }
    }

    /** Un élément de paie résolu pour la période, en dollars, avec son traitement fiscal. */
    public record ElementResolu(String kind, String label, double employeeAmount, double employerAmount, boolean reducesFederalTax, boolean reducesQuebecTax,
                                boolean taxableFederal, boolean taxableQuebec) {
        public Map<String, Object> vue() {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("kind", kind);
            m.put("label", label);
            m.put("employeeAmount", employeeAmount);
            m.put("employerAmount", employerAmount);
            m.put("reducesFederalTax", reducesFederalTax);
            m.put("reducesQuebecTax", reducesQuebecTax);
            m.put("taxableFederal", taxableFederal);
            m.put("taxableQuebec", taxableQuebec);
            return m;
        }
    }

    /** Ce qu'une paie reçoit ; {@code sansRetenues} = travailleur indépendant ou hors Canada, net = brut. */
    public record Entree(double grossSalary, String frequency, Ytd ytdBefore, double benefitEmployeeDeduction, double benefitEmployerCost, double benefitTaxableAmount,
                         List<ElementResolu> deductionItems, boolean sansRetenues) {
        public static Entree simple(double gross, String frequency, Ytd ytd) {
            return new Entree(gross, frequency, ytd, 0, 0, 0, List.of(), false);
        }
    }

    /** Le bulletin : retenues de l'employé, charges de l'employeur, cumul après. */
    public record Bulletin(double gross, String frequency, int periodsPerYear, double federalTax, double quebecTax, double qpp, double qpp2, double ei, double qpip,
                           double benefitDeduction, double benefitEmployerCost, double benefitTaxable, double totalEmployeeDeductions, double netPay,
                           double employerQpp, double employerQpp2, double employerEi, double employerQpip, double employerCnesst, double employerFss, double totalEmployerCost,
                           Ytd ytdAfter, Double itemEmployeeDeductions, Double itemEmployerCost, List<ElementResolu> deductionItems) {

        public Map<String, Object> vue() {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("gross", gross);
            m.put("frequency", frequency);
            m.put("periodsPerYear", periodsPerYear);
            m.put("federalTax", federalTax);
            m.put("quebecTax", quebecTax);
            m.put("qpp", qpp);
            m.put("qpp2", qpp2);
            m.put("ei", ei);
            m.put("qpip", qpip);
            m.put("benefitDeduction", benefitDeduction);
            m.put("benefitEmployerCost", benefitEmployerCost);
            m.put("benefitTaxable", benefitTaxable);
            m.put("totalEmployeeDeductions", totalEmployeeDeductions);
            m.put("netPay", netPay);
            m.put("employerQpp", employerQpp);
            m.put("employerQpp2", employerQpp2);
            m.put("employerEi", employerEi);
            m.put("employerQpip", employerQpip);
            m.put("employerCnesst", employerCnesst);
            m.put("employerFss", employerFss);
            m.put("totalEmployerCost", totalEmployerCost);
            m.put("ytdAfter", ytdAfter.vue());
            if (itemEmployeeDeductions != null) {
                m.put("itemEmployeeDeductions", itemEmployeeDeductions);
                m.put("itemEmployerCost", itemEmployerCost);
                if (deductionItems != null && !deductionItems.isEmpty()) {
                    m.put("deductionItems", deductionItems.stream().map(ElementResolu::vue).toList());
                }
            }
            return m;
        }

        /** Relit un bulletin journalisé ; les champs absents valent zéro. */
        @SuppressWarnings("unchecked")
        public static Bulletin depuis(Map<String, Object> m) {
            List<ElementResolu> elements = new ArrayList<>();
            if (m.get("deductionItems") instanceof List<?> l) {
                for (Object o : l) {
                    Map<String, Object> e = (Map<String, Object>) o;
                    elements.add(new ElementResolu((String) e.get("kind"), (String) e.get("label"), nombre(e.get("employeeAmount")), nombre(e.get("employerAmount")),
                            Boolean.TRUE.equals(e.get("reducesFederalTax")), Boolean.TRUE.equals(e.get("reducesQuebecTax")), Boolean.TRUE.equals(e.get("taxableFederal")), Boolean.TRUE.equals(e.get("taxableQuebec"))));
                }
            }
            return new Bulletin(nombre(m.get("gross")), (String) m.get("frequency"), (int) nombre(m.get("periodsPerYear")), nombre(m.get("federalTax")), nombre(m.get("quebecTax")),
                    nombre(m.get("qpp")), nombre(m.get("qpp2")), nombre(m.get("ei")), nombre(m.get("qpip")), nombre(m.get("benefitDeduction")), nombre(m.get("benefitEmployerCost")),
                    nombre(m.get("benefitTaxable")), nombre(m.get("totalEmployeeDeductions")), nombre(m.get("netPay")), nombre(m.get("employerQpp")), nombre(m.get("employerQpp2")),
                    nombre(m.get("employerEi")), nombre(m.get("employerQpip")), nombre(m.get("employerCnesst")), nombre(m.get("employerFss")), nombre(m.get("totalEmployerCost")),
                    Ytd.depuis(m.get("ytdAfter") instanceof Map<?, ?> y ? (Map<String, Object>) y : null),
                    m.get("itemEmployeeDeductions") == null ? null : nombre(m.get("itemEmployeeDeductions")), m.get("itemEmployerCost") == null ? null : nombre(m.get("itemEmployerCost")), elements);
        }
    }

    static double nombre(Object v) {
        return v instanceof Number n ? n.doubleValue() : 0;
    }

    public static double arrondir(double n) {
        return Math.round(n * 100.0) / 100.0;
    }

    /** L'impôt d'un barème progressif sur un revenu annuel. */
    public static double impotProgressif(double annuel, List<Baremes.Tranche> tranches) {
        if (annuel <= 0) {
            return 0;
        }
        double impot = 0;
        double precedent = 0;
        for (Baremes.Tranche t : tranches) {
            double plafond = t.upTo() == null ? Double.POSITIVE_INFINITY : t.upTo();
            if (annuel > plafond) {
                impot += (plafond - precedent) * t.rate();
                precedent = plafond;
            } else {
                return impot + (annuel - precedent) * t.rate();
            }
        }
        return impot;
    }

    public static int periodesParAn(String frequence) {
        Integer n = Baremes.PERIODES_PAR_AN.get(frequence);
        if (n == null) {
            throw new IllegalArgumentException("frequency must be one of " + String.join(",", Baremes.PERIODES_PAR_AN.keySet()));
        }
        return n;
    }

    private static double somme(List<ElementResolu> elements, ToDoubleFunction<ElementResolu> f) {
        return arrondir(elements.stream().mapToDouble(f).sum());
    }

    public static Bulletin calculer(Entree e, Taux taux) {
        if (e.grossSalary() < 0) {
            throw new IllegalArgumentException("grossSalary must be >= 0");
        }
        Ytd ytd = e.ytdBefore() == null ? Ytd.ZERO : e.ytdBefore();
        int periodes = periodesParAn(e.frequency());
        double brut = e.grossSalary();
        if (e.sansRetenues()) {
            double g = arrondir(brut);
            double chargeAvantages = arrondir(e.benefitEmployerCost());
            return new Bulletin(g, e.frequency(), periodes, 0, 0, 0, 0, 0, 0, 0, chargeAvantages, arrondir(e.benefitTaxableAmount()), 0, g, 0, 0, 0, 0, 0, 0,
                    arrondir(g + chargeAvantages), ytd, null, null, List.of());
        }
        List<ElementResolu> elements = e.deductionItems() == null ? List.of() : e.deductionItems();
        double reductionFed = somme(elements, i -> i.reducesFederalTax() ? i.employeeAmount() : 0);
        double reductionQc = somme(elements, i -> i.reducesQuebecTax() ? i.employeeAmount() : 0);
        double avantageFed = somme(elements, i -> i.taxableFederal() ? i.employerAmount() : 0);
        double avantageQc = somme(elements, i -> i.taxableQuebec() ? i.employerAmount() : 0);
        double retenuesElements = somme(elements, ElementResolu::employeeAmount);
        double chargeElements = somme(elements, ElementResolu::employerAmount);
        double ajoutRrq = somme(elements, i -> i.taxableFederal() || i.taxableQuebec() ? i.employerAmount() : 0);
        double ajoutAe = avantageFed;
        double ajoutRqap = avantageQc;

        double baseFederale = Math.max(0, brut - reductionFed + avantageFed) * periodes;
        double baseQuebec = Math.max(0, brut - reductionQc + avantageQc) * periodes;
        double federalBrut = impotProgressif(baseFederale, taux.tranchesFederales());
        double creditFederal = taux.montantPersonnelFederal() * taux.tranchesFederales().get(0).rate();
        double federalTax = arrondir(Math.max(0, federalBrut - creditFederal) * (1 - taux.abattementQuebec()) / periodes);
        double qcBrut = impotProgressif(baseQuebec, taux.tranchesQuebec());
        double creditQc = taux.montantPersonnelQuebec() * taux.tranchesQuebec().get(0).rate();
        double quebecTax = arrondir(Math.max(0, qcBrut - creditQc) / periodes);

        double gainsRrq = brut + ajoutRrq;
        double resteRrq = Math.max(0, taux.maxRrq() - ytd.qppPensionable());
        double rrqPeriode = Math.min(gainsRrq, resteRrq);
        double qpp = arrondir(Math.max(0, rrqPeriode - taux.exemptionRrq() / periodes) * taux.tauxRrq());
        double cumulRrq2 = ytd.qppPensionable() + rrqPeriode;
        double rrq2Periode = Math.max(0, Math.min(cumulRrq2, taux.maxRrq2()) - Math.max(taux.maxRrq(), cumulRrq2 - gainsRrq));
        double qpp2 = arrondir(rrq2Periode * taux.tauxRrq2());

        double aePeriode = Math.min(brut + ajoutAe, Math.max(0, taux.maxAe() - ytd.eiInsurable()));
        double ei = arrondir(aePeriode * taux.tauxAe());
        double rqapPeriode = Math.min(brut + ajoutRqap, Math.max(0, taux.maxRqap() - ytd.qpipInsurable()));
        double qpip = arrondir(rqapPeriode * taux.tauxRqapEmploye());

        double totalRetenues = arrondir(federalTax + quebecTax + qpp + qpp2 + ei + qpip);
        double retenueAvantages = arrondir(e.benefitEmployeeDeduction());
        double chargeAvantages = arrondir(e.benefitEmployerCost());
        double net = arrondir(brut - totalRetenues - retenueAvantages - retenuesElements);
        double employerEi = arrondir(ei * taux.multiplicateurAeEmployeur());
        double employerQpip = arrondir(rqapPeriode * taux.tauxRqapEmployeur());
        double employerCnesst = arrondir(brut * taux.tauxCnesst());
        double employerFss = arrondir(brut * taux.tauxFss());
        double totalEmployeur = arrondir(brut + qpp + qpp2 + employerEi + employerQpip + employerCnesst + employerFss + chargeAvantages + chargeElements);
        Ytd apres = new Ytd(arrondir(ytd.qppPensionable() + rrqPeriode), arrondir(ytd.qppContribution() + qpp), arrondir(ytd.qpp2Contribution() + qpp2),
                arrondir(ytd.eiInsurable() + aePeriode), arrondir(ytd.eiContribution() + ei), arrondir(ytd.qpipInsurable() + rqapPeriode), arrondir(ytd.qpipContribution() + qpip));
        return new Bulletin(arrondir(brut), e.frequency(), periodes, federalTax, quebecTax, qpp, qpp2, ei, qpip, retenueAvantages, chargeAvantages, arrondir(e.benefitTaxableAmount()),
                totalRetenues, net, qpp, qpp2, employerEi, employerQpip, employerCnesst, employerFss, totalEmployeur, apres, retenuesElements, chargeElements, elements);
    }

    /**
     * Un bulletin émis et daté ({@code PayRunRecord}), porté en version SLIM : sans le champ d'absences
     * (couplé à un autre contexte, hors périmètre RH-1). Suffit aux déclarations (date de paie, employé, slip).
     */
    public record BulletinEmis(String id, String employeeId, String periodStart, String periodEnd, String payDate,
                               Bulletin slip, String createdAt, String tenantId, String currency, String country, String source) {
    }
}
