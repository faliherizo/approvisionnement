package qc.levis.contrat.paie.domain;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Les remises de retenues à la source aux gouvernements — Revenu Québec (TPZ-1015.R.14) et
 * ARC (PD7A) : générées d'une période de paie, échéancées selon la fréquence du remettant,
 * transmises (simulées à défaut d'un point de dépôt configuré), exportées en XML et en fichier
 * de paiement CPA-005.
 *
 * Portage de {@code payroll/remittances.ts}, {@code remittanceSchedule.ts} et {@code forms/*}.
 * Aucune API officielle n'existe : le dépôt réel passe par clicSÉQUR Entreprises, Mon dossier
 * d'entreprise, les institutions financières ou un PSP certifié.
 */
public final class Remises {

    private Remises() {
    }

    public static final double SEUIL_ACCELERE_2 = 100_000;
    public static final double SEUIL_ACCELERE_1 = 25_000;
    public static final double MAX_TRIMESTRIEL = 1_000;
    private static final Pattern DATE = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");

    public record Remise(String id, String authority, String periodStart, String periodEnd, double amount, Map<String, Double> breakdown, String status, String submittedAt, String confirmationNumber,
                         String errorMessage, String createdAt, String tenantId) {
        /** L'échéance n'est pas stockée : elle se calcule, au régime régulier. */
        public String dueDate() {
            return echeance(periodEnd, "regular");
        }

        public boolean confirmee() {
            return "confirmed".equals(status);
        }

        public Remise soumiseLe(String instant) {
            return new Remise(id, authority, periodStart, periodEnd, amount, breakdown, status, instant, confirmationNumber, errorMessage, createdAt, tenantId);
        }

        /** Confirmée ; une transmission réelle efface l'erreur précédente, la simulation la laisse — comme au Node. */
        public Remise confirmer(String confirmation, boolean effacerErreur) {
            return new Remise(id, authority, periodStart, periodEnd, amount, breakdown, "confirmed", submittedAt, confirmation, effacerErreur ? null : errorMessage, createdAt, tenantId);
        }

        public Remise echouer(String erreur) {
            return new Remise(id, authority, periodStart, periodEnd, amount, breakdown, "failed", submittedAt, confirmationNumber, erreur, createdAt, tenantId);
        }

        public String formulaire() {
            return "RQ".equals(authority) ? "TPZ-1015.R.14" : "PD7A";
        }
    }

    public record Echeance(String authority, String periodStart, String periodEnd, String dueDate, String frequency) {
    }

    public record Rappel(String id, String authority, String dueDate, double amount, String status, long daysUntilDue, boolean overdue) {
    }

    public record Employeur(String name, String identifier, String neq, String bn, String address) {
    }

    public record CoordonneesBancaires(String name, String institution, String transit, String account) {
    }

    public record Emetteur(String originatorId, String shortName, String institution, String transit, String account) {
    }

    public record Transmise(boolean ok, String confirmationNumber, String errorMessage) {
    }

    public interface Depot {
        List<Remise> lister(String tenantId, String authority);

        Optional<Remise> lire(String id);

        void ajouter(Remise r);

        void sauver(Remise r);
    }

    /** Port : le point de dépôt (PSP, EDI, mandataire) ; en simulation, une confirmation locale. */
    public interface Transmission {
        Transmise soumettre(String authority, String xml, String idempotencyKey, boolean simulate);
    }

    private static double r2(double n) {
        return Math.round(n * 100) / 100.0;
    }

    public static boolean estDate(String s) {
        return s != null && DATE.matcher(s).matches();
    }

    /** {@code computeDueDate} : le 15 du mois suivant au régime régulier et trimestriel ; accéléré 1 le 25 ou le 10 ; accéléré 2 trois jours après. */
    public static String echeance(String periodEnd, String frequency) {
        LocalDate d;
        try {
            d = LocalDate.parse(periodEnd);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("invalid periodEnd");
        }
        return switch (frequency == null ? "regular" : frequency) {
            case "accelerated1" -> d.getDayOfMonth() <= 15 ? d.withDayOfMonth(25).toString() : d.plusMonths(1).withDayOfMonth(10).toString();
            case "accelerated2" -> d.plusDays(3).toString();
            default -> d.plusMonths(1).withDayOfMonth(15).toString();
        };
    }

    /** La fréquence d'un remettant selon sa moyenne mensuelle de retenues ; le trimestriel exige une conformité parfaite. */
    public static String frequence(double amwa, boolean conformiteParfaite) {
        double v = Double.isNaN(amwa) ? 0 : amwa;
        if (v >= SEUIL_ACCELERE_2) {
            return "accelerated2";
        }
        if (v >= SEUIL_ACCELERE_1) {
            return "accelerated1";
        }
        if (v < MAX_TRIMESTRIEL && conformiteParfaite) {
            return "quarterly";
        }
        return "regular";
    }

    /** L'échéancier annuel : douze périodes mensuelles, ou quatre trimestres. */
    public static List<Echeance> echeancier(String authority, String frequency, double year) {
        if (year != Math.rint(year) || year < 2000) {
            throw new IllegalArgumentException("year must be a valid calendar year");
        }
        int a = (int) year;
        List<Echeance> sortie = new ArrayList<>();
        int pas = "quarterly".equals(frequency) ? 3 : 1;
        for (int m = 1; m <= 12; m += pas) {
            String debut = LocalDate.of(a, m, 1).toString();
            String fin = YearMonth.of(a, m + pas - 1).atEndOfMonth().toString();
            sortie.add(new Echeance(authority, debut, fin, echeance(fin, frequency), frequency));
        }
        return sortie;
    }

    /** Les remises en attente en retard ou dues sous {@code withinDays}, les plus urgentes d'abord. */
    public static List<Rappel> rappels(List<Remise> remises, String asOf, double withinDays) {
        if (!estDate(asOf)) {
            throw new IllegalArgumentException("asOf must be YYYY-MM-DD");
        }
        return remises.stream().filter(r -> "pending".equals(r.status())).map(r -> {
            long jours = ChronoUnit.DAYS.between(LocalDate.parse(asOf), LocalDate.parse(r.dueDate()));
            return new Rappel(r.id(), r.authority(), r.dueDate(), r.amount(), r.status(), jours, jours < 0);
        }).filter(r -> r.overdue() || r.daysUntilDue() <= withinDays).sorted(Comparator.comparingLong(Rappel::daysUntilDue)).toList();
    }

    /** {@code createRemittancesFromPeriod} : une remise par autorité dont le total est positif, jamais deux pour la même période. */
    public static List<Remise> depuisRapport(Declarations.RapportRetenues rapport, List<Remise> existantes, java.util.function.Supplier<String> ids, String tenantId, String instant) {
        List<Remise> creees = new ArrayList<>();
        boolean rqExiste = existantes.stream().anyMatch(r -> "RQ".equals(r.authority()) && memePeriode(r, rapport.period()));
        boolean craExiste = existantes.stream().anyMatch(r -> "CRA".equals(r.authority()) && memePeriode(r, rapport.period()));
        if (rapport.totalToRevenuQuebec() > 0 && !rqExiste) {
            Map<String, Double> detail = new LinkedHashMap<>();
            detail.put("quebecIncomeTax", rapport.quebecIncomeTax());
            detail.put("qpp", rapport.qpp());
            detail.put("qpp2", rapport.qpp2());
            detail.put("qpip", rapport.qpip());
            detail.put("fss", rapport.fss());
            creees.add(new Remise(ids.get(), "RQ", rapport.period().start(), rapport.period().end(), r2(rapport.totalToRevenuQuebec()), detail, "pending", null, null, null, instant, tenantId));
        }
        if (rapport.totalToCRA() > 0 && !craExiste) {
            Map<String, Double> detail = new LinkedHashMap<>();
            detail.put("federalIncomeTax", rapport.federalIncomeTax());
            detail.put("ei", rapport.ei());
            creees.add(new Remise(ids.get(), "CRA", rapport.period().start(), rapport.period().end(), r2(rapport.totalToCRA()), detail, "pending", null, null, null, instant, tenantId));
        }
        return creees;
    }

    private static boolean memePeriode(Remise r, Declarations.Periode p) {
        return r.periodStart().equals(p.start()) && r.periodEnd().equals(p.end());
    }

    // ── XML ───────────────────────────────────────────────────────────────

    public static String echapperXml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;");
    }

    private static String balise(String nom, Object valeur, String... attributs) {
        StringBuilder attrs = new StringBuilder();
        for (int i = 0; i + 1 < attributs.length; i += 2) {
            attrs.append(' ').append(attributs[i]).append("=\"").append(echapperXml(attributs[i + 1])).append('"');
        }
        if (valeur == null || "".equals(valeur)) {
            return "<" + nom + attrs + "/>";
        }
        String texte = valeur instanceof Number n ? String.format(java.util.Locale.ROOT, "%.2f", n.doubleValue()) : echapperXml(String.valueOf(valeur));
        return "<" + nom + attrs + ">" + texte + "</" + nom + ">";
    }

    /** L'XML de dépôt : TPZ-1015.R.14 pour Revenu Québec, PD7A avec en-tête T619 pour l'ARC. */
    public static String xml(Employeur employeur, Remise r, Declarations.RapportRetenues rapport, String genereLe) {
        String due = echeance(r.periodEnd(), "regular");
        if ("RQ".equals(r.authority())) {
            return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TPZ1015R14 schemaVersion=\"1.0\" generatedAt=\"" + echapperXml(genereLe) + "\">\n  <Employer>\n    " + balise("Name", employeur.name()) + "\n    "
                    + balise("NEQ", employeur.identifier()) + "\n    " + balise("Address", employeur.address()) + "\n  </Employer>\n  <Period>\n    " + balise("Start", r.periodStart()) + "\n    " + balise("End", r.periodEnd())
                    + "\n    " + balise("DueDate", due) + "\n  </Period>\n  <Amounts currency=\"CAD\">\n    " + balise("GrossPayroll", rapport.totalGross()) + "\n    " + balise("QuebecIncomeTax", rapport.quebecIncomeTax(), "box", "20")
                    + "\n    " + balise("QPP", rapport.qpp(), "box", "30") + "\n    " + balise("QPP2", rapport.qpp2(), "box", "30A") + "\n    " + balise("QPIP", rapport.qpip(), "box", "31") + "\n    " + balise("FSS", rapport.fss(), "box", "70")
                    + "\n    " + balise("Total", rapport.totalToRevenuQuebec()) + "\n  </Amounts>\n" + soumission(r) + "</TPZ1015R14>\n";
        }
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<PD7A schemaVersion=\"1.0\" generatedAt=\"" + echapperXml(genereLe) + "\">\n  <T619>\n    " + balise("TransmitterName", employeur.name()) + "\n    "
                + balise("BN", employeur.identifier()) + "\n    " + balise("Address", employeur.address()) + "\n    " + balise("GeneratedAt", genereLe) + "\n  </T619>\n  <Period>\n    " + balise("Start", r.periodStart()) + "\n    "
                + balise("End", r.periodEnd()) + "\n    " + balise("DueDate", due) + "\n  </Period>\n  <Amounts currency=\"CAD\">\n    " + balise("GrossPayroll", rapport.totalGross()) + "\n    "
                + balise("FederalIncomeTax", rapport.federalIncomeTax()) + "\n    " + balise("EI", rapport.ei()) + "\n    " + balise("Total", rapport.totalToCRA()) + "\n  </Amounts>\n" + soumission(r) + "</PD7A>\n";
    }

    private static String soumission(Remise r) {
        return "  <Submission>\n    " + balise("RemittanceId", r.id()) + "\n    " + balise("Status", r.status()) + "\n    " + balise("ConfirmationNumber", r.confirmationNumber()) + "\n    " + balise("SubmittedAt", r.submittedAt())
                + "\n  </Submission>\n";
    }

    // ── CPA-005 ───────────────────────────────────────────────────────────

    private static String remplir(String s, int n, char c, boolean gauche) {
        String t = s.length() > n ? s.substring(0, n) : s;
        String bourrage = String.valueOf(c).repeat(n - t.length());
        return gauche ? bourrage + t : t + bourrage;
    }

    private static String num(double n, int longueur) {
        return remplir(String.valueOf(Math.round(n)), longueur, '0', true);
    }

    private static String alpha(String s, int longueur) {
        String nettoye = Normalizer.normalize(s.toUpperCase(java.util.Locale.ROOT), Normalizer.Form.NFD).replaceAll("[\\p{InCombiningDiacriticalMarks}]", "").replaceAll("[^A-Z0-9 ./()&-]", " ");
        return remplir(nettoye, longueur, ' ', false);
    }

    /** YYYY-MM-DD → 0YYJJJ (julien CPA, six caractères). */
    public static String julien(String date) {
        LocalDate d = LocalDate.parse(date);
        return "0" + String.valueOf(d.getYear()).substring(2) + remplir(String.valueOf(d.getDayOfYear()), 3, '0', true);
    }

    /** Le fichier EFT CPA-005 (Paiements Canada) : en-tête A, un crédit C, sommaire Z — 1 500 caractères chacun. */
    public static String cpa005(Emetteur emetteur, CoordonneesBancaires beneficiaire, String numeroFichier, String dateCreation, String dateEcheance, double montant, String reference) {
        long cents = Math.round(montant * 100);
        String a = "A" + num(1, 9) + remplir(emetteur.originatorId(), 10, ' ', false) + remplir(numeroFichier, 4, '0', true) + julien(dateCreation) + "00000" + "CAD" + remplir("", 1462, ' ', false);
        String c = "C" + num(2, 9) + remplir(emetteur.originatorId(), 10, ' ', false) + remplir(numeroFichier, 4, '0', true) + "200" + num(cents, 10) + julien(dateEcheance) + remplir(beneficiaire.institution(), 4, '0', true)
                + remplir(beneficiaire.transit(), 5, '0', true) + remplir(beneficiaire.account(), 12, ' ', false) + remplir("", 22, ' ', false) + remplir("", 3, ' ', false) + "0" + alpha(beneficiaire.name(), 30)
                + alpha(emetteur.shortName(), 30) + alpha(reference, 19) + remplir(emetteur.institution(), 4, '0', true) + remplir(emetteur.transit(), 5, '0', true) + remplir(emetteur.account(), 12, ' ', false)
                + remplir("", 15, ' ', false) + remplir("", 22, ' ', false) + "CA" + remplir("", 11, ' ', false);
        c = remplir(c, 1500, ' ', false);
        String z = "Z" + num(3, 9) + remplir(emetteur.originatorId(), 10, ' ', false) + remplir(numeroFichier, 4, '0', true) + num(cents, 14) + num(1, 8) + num(0, 14) + num(0, 8) + remplir("", 1432, ' ', false);
        return a + "\n" + c + "\n" + z + "\n";
    }
}
