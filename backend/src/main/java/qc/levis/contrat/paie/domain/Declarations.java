package qc.levis.contrat.paie.domain;

import java.util.List;
import java.util.function.ToDoubleFunction;

/**
 * Les montants attendus dans les déclarations périodiques : TPS/TVQ (FPZ-500), retenues à la
 * source (TPZ-1015.R.14 à Revenu Québec, PD7A à l'ARC), synthèse annuelle Relevé 1 et T4.
 *
 * Portage de {@code payroll/reports.ts}. Ce module produit les montants ; le dépôt officiel se
 * fait par clicSÉQUR Entreprises ou Mon dossier d'entreprise — jamais d'ici.
 */
public final class Declarations {

    private Declarations() {
    }

    public record Periode(String start, String end) {
    }

    public record RapportTaxes(Periode period, double gstCollected, double qstCollected, double gstITC, double qstITR, double gstNet, double qstNet) {
    }

    public record RapportRetenues(Periode period, int payRunCount, double totalGross, double quebecIncomeTax, double qpp, double qpp2, double qpip, double fss, double totalToRevenuQuebec,
                                  double federalIncomeTax, double ei, double totalToCRA) {
    }

    public record Releve1(double caseA_employmentIncome, double caseB_qpp, double caseB1_qpp2, double caseC_ei, double caseD_rrsp, double caseE_quebecTax, double caseG_qpp_pensionable, double caseH_qpip) {
    }

    public record T4(double box14_employmentIncome, double box16_qpp, double box17_qpp2, double box18_ei, double box22_federalTax) {
    }

    public record SyntheseAnnuelle(String employeeId, int year, int payRunCount, Releve1 rl1, T4 t4) {
    }

    /** L'arrondi laisse passer NaN : un montant illisible reste illisible, il ne devient pas zéro. */
    private static double r2(double n) {
        return Double.isNaN(n) ? n : Math.round(n * 100) / 100.0;
    }

    /** FPZ-500 : taxes perçues données, CTI/RTI des dépenses de la période au prorata déductible, net à verser ou à recevoir. */
    public static RapportTaxes taxes(Periode p, List<Depenses.Depense> depenses, double gstCollected, double qstCollected) {
        Depenses.Sommaire s = Depenses.sommaire(depenses.stream().filter(e -> e.date().compareTo(p.start()) >= 0 && e.date().compareTo(p.end()) <= 0).toList(), 0.3);
        return new RapportTaxes(p, r2(gstCollected), r2(qstCollected), s.recoverableGST(), s.recoverableQST(), r2(gstCollected - s.recoverableGST()), r2(qstCollected - s.recoverableQST()));
    }

    /** Les retenues à remettre sur les bulletins dont la date de paie tombe dans la période : parts employé et employeur réunies. */
    public static RapportRetenues retenues(Periode p, List<MoteurPaie.BulletinEmis> bulletins) {
        List<MoteurPaie.BulletinEmis> dansLaPeriode = bulletins.stream().filter(r -> r.payDate().compareTo(p.start()) >= 0 && r.payDate().compareTo(p.end()) <= 0).toList();
        ToDoubleFunction<ToDoubleFunction<MoteurPaie.Bulletin>> somme = f -> dansLaPeriode.stream().mapToDouble(r -> f.applyAsDouble(r.slip())).sum();
        double brut = somme.applyAsDouble(MoteurPaie.Bulletin::gross);
        double impotQuebec = somme.applyAsDouble(MoteurPaie.Bulletin::quebecTax);
        double rrq = somme.applyAsDouble(s -> s.qpp() + s.employerQpp());
        double rrq2 = somme.applyAsDouble(s -> s.qpp2() + s.employerQpp2());
        double rqap = somme.applyAsDouble(s -> s.qpip() + s.employerQpip());
        double fss = somme.applyAsDouble(MoteurPaie.Bulletin::employerFss);
        double impotFederal = somme.applyAsDouble(MoteurPaie.Bulletin::federalTax);
        double ae = somme.applyAsDouble(s -> s.ei() + s.employerEi());
        return new RapportRetenues(p, dansLaPeriode.size(), r2(brut), r2(impotQuebec), r2(rrq), r2(rrq2), r2(rqap), r2(fss), r2(impotQuebec + rrq + rrq2 + rqap + fss), r2(impotFederal), r2(ae), r2(impotFederal + ae));
    }

    /** Relevé 1 et T4 d'une personne pour une année : les bulletins dont la date de paie est de cette année. */
    public static SyntheseAnnuelle finDAnnee(String employeeId, List<MoteurPaie.BulletinEmis> bulletins, int annee) {
        List<MoteurPaie.BulletinEmis> runs = bulletins.stream().filter(r -> r.employeeId().equals(employeeId) && r.payDate().startsWith(annee + "-")).toList();
        ToDoubleFunction<ToDoubleFunction<MoteurPaie.Bulletin>> somme = f -> r2(runs.stream().mapToDouble(r -> f.applyAsDouble(r.slip())).sum());
        double brut = somme.applyAsDouble(MoteurPaie.Bulletin::gross);
        double rrq = somme.applyAsDouble(MoteurPaie.Bulletin::qpp);
        double rrq2 = somme.applyAsDouble(MoteurPaie.Bulletin::qpp2);
        double ae = somme.applyAsDouble(MoteurPaie.Bulletin::ei);
        // La case G est approchée par le brut : le plafonnement au MGAP reste à faire, comme au Node.
        return new SyntheseAnnuelle(employeeId, annee, runs.size(), new Releve1(brut, rrq, rrq2, ae, 0, somme.applyAsDouble(MoteurPaie.Bulletin::quebecTax), brut, somme.applyAsDouble(MoteurPaie.Bulletin::qpip)),
                new T4(brut, rrq, rrq2, ae, somme.applyAsDouble(MoteurPaie.Bulletin::federalTax)));
    }
}
