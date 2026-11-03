package qc.levis.contrat.paie.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import qc.levis.contrat.paie.application.dto.ApercuPaieRequest;
import qc.levis.contrat.paie.domain.Baremes;
import qc.levis.contrat.paie.domain.Declarations;
import qc.levis.contrat.paie.domain.Depenses;
import qc.levis.contrat.paie.domain.ImpotSurLeRevenu;
import qc.levis.contrat.paie.domain.MoteurPaie;
import qc.levis.contrat.paie.domain.Remises;

/**
 * Cas d'utilisation de la paie : s'appuie sur les moteurs PURS portés de Nexu VINA (MoteurPaie, Baremes,
 * ImpotSurLeRevenu, Declarations, Depenses). Aucune persistance (RH-1) : calcul/aperçu uniquement. Les
 * bulletins des déclarations sont recalculés à partir d'entrées fournies (pas de registre de paie ici).
 */
@Service
public class PaieService {

    /** Aperçu d'un bulletin pour un brut et une fréquence donnés (barèmes 2025). */
    public Map<String, Object> apercu(ApercuPaieRequest req) {
        MoteurPaie.Entree entree = req.sansRetenues()
                ? new MoteurPaie.Entree(req.grossSalary(), freq(req.frequency()), MoteurPaie.Ytd.ZERO, 0, 0, 0, List.of(), true)
                : MoteurPaie.Entree.simple(req.grossSalary(), freq(req.frequency()), MoteurPaie.Ytd.ZERO);
        return MoteurPaie.calculer(entree, MoteurPaie.Taux.DE_2025).vue();
    }

    public Map<String, Object> taux() {
        return Baremes.taux2025();
    }

    public List<Map<String, Object>> frequences() {
        return Baremes.frequences();
    }

    /** Impôt sur le revenu (société / autonome / salarié) — ImpotSurLeRevenu porté. */
    public Map<String, Object> impot(Map<String, Object> e) {
        ImpotSurLeRevenu.validerApercu(e);
        return ImpotSurLeRevenu.vue(ImpotSurLeRevenu.calculer(e));
    }

    /** FPZ-500 (TPS/TVQ) : taxes perçues − CTI/RTI des dépenses de la période. */
    public Declarations.RapportTaxes taxes(Map<String, Object> body) {
        return Declarations.taxes(periode(body), depenses(body.get("depenses")),
                ImpotSurLeRevenu.nombre(body.get("gstCollected")), ImpotSurLeRevenu.nombre(body.get("qstCollected")));
    }

    /** Retenues à la source (TPZ-1015.R.14 / PD7A) sur des bulletins recalculés à partir d'entrées de paie. */
    public Declarations.RapportRetenues retenues(Map<String, Object> body) {
        return Declarations.retenues(periode(body), bulletins(body.get("bulletins")));
    }

    /** Synthèse Relevé 1 + T4 d'un employé pour une année, sur des bulletins recalculés. */
    public Declarations.SyntheseAnnuelle finDAnnee(Map<String, Object> body) {
        int annee = (int) ImpotSurLeRevenu.nombre(body.get("annee"));
        return Declarations.finDAnnee(String.valueOf(body.get("employeeId")), bulletins(body.get("bulletins")), annee);
    }

    /** Échéancier de remise des retenues à la source pour une autorité et une fréquence données. */
    public List<Remises.Echeance> echeancierRemises(String authority, String frequency, int year) {
        return Remises.echeancier(authority, frequency == null ? "monthly" : frequency, year);
    }

    // --- interne ---

    private Declarations.Periode periode(Map<String, Object> body) {
        return new Declarations.Periode(String.valueOf(body.get("start")), String.valueOf(body.get("end")));
    }

    private List<Depenses.Depense> depenses(Object liste) {
        List<Depenses.Depense> res = new ArrayList<>();
        if (liste instanceof List<?> l) {
            for (Object o : l) {
                if (o instanceof Map<?, ?> m) {
                    double ht = ImpotSurLeRevenu.nombre(m.get("amountHT"));
                    String cat = m.get("category") == null ? "other" : String.valueOf(m.get("category"));
                    double pct = m.get("deductiblePct") instanceof Number p ? p.doubleValue() : Depenses.deductibiliteParDefaut(cat);
                    Depenses.Taxes tx = m.get("gst") instanceof Number g && m.get("qst") instanceof Number q
                            ? new Depenses.Taxes(g.doubleValue(), q.doubleValue(), 0) : Depenses.taxesDeHT(ht);
                    String desc = m.get("description") == null ? "" : String.valueOf(m.get("description"));
                    res.add(new Depenses.Depense(null, String.valueOf(m.get("date")), cat, desc,
                            ht, tx.gst(), tx.qst(), pct, null, null, null, null, null, null, null, null, null, null));
                }
            }
        }
        return res;
    }

    private List<MoteurPaie.BulletinEmis> bulletins(Object liste) {
        List<MoteurPaie.BulletinEmis> res = new ArrayList<>();
        if (liste instanceof List<?> l) {
            for (Object o : l) {
                if (o instanceof Map<?, ?> m) {
                    double gross = ImpotSurLeRevenu.nombre(m.get("grossSalary"));
                    String frequency = freq(m.get("frequency") == null ? null : String.valueOf(m.get("frequency")));
                    MoteurPaie.Bulletin slip = MoteurPaie.calculer(MoteurPaie.Entree.simple(gross, frequency, MoteurPaie.Ytd.ZERO), MoteurPaie.Taux.DE_2025);
                    res.add(new MoteurPaie.BulletinEmis(null, String.valueOf(m.get("employeeId")), null, null,
                            String.valueOf(m.get("payDate")), slip, null, null, "CAD", "CA", "preview"));
                }
            }
        }
        return res;
    }

    private String freq(String f) {
        return f == null || f.isBlank() ? "biweekly" : f;
    }
}
