package qc.levis.contrat.paie.domain;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Les dépenses d'affaires et leur portée fiscale — Revenu Québec et ARC : repas et représentation
 * déductibles à 50 %, le reste à 100 % quand l'usage est professionnel ; TPS et TVQ récupérables
 * (CTI / RTI) au prorata de la part déductible.
 *
 * Portage de {@code payroll/expenses.ts} et {@code expenseAttachments.ts}. Une dépense
 * « document » porte des lignes catégorisées et poste une écriture équilibrée au grand livre :
 * débit des charges et des taxes à recouvrer, crédit du compte de paiement.
 */
public final class Depenses {

    private Depenses() {
    }

    public static final double TAUX_TPS = 0.05;
    public static final double TAUX_TVQ = 0.09975;
    private static final Pattern DATE = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
    private static final long TAILLE_MAX = 50L * 1024 * 1024;

    public record Categorie(String key, String code, String label, double deductiblePct) {
    }

    /** Le plan de comptes de dépenses ; la clé est stockée sur la ligne, le code est le compte de charges. */
    public static final List<Categorie> CATEGORIES = List.of(
            new Categorie("insurance", "6000", "Assurance", 100), new Categorie("insurance_disability", "6010", "Assurance - Invalidité", 100), new Categorie("insurance_liability", "6020", "Assurance - Responsabilité", 100),
            new Categorie("admin", "6100", "Autres dépenses générales et administratives", 100), new Categorie("commissions", "6200", "Commissions et frais", 100), new Categorie("bad_debts", "6300", "Créances irrécouvrables", 100),
            new Categorie("dues", "6400", "Droits d'adhésion et abonnements", 100), new Categorie("office", "6500", "Fournitures de bureau", 100), new Categorie("software", "6510", "Logiciels et abonnements", 100),
            new Categorie("telecom", "6520", "Télécommunications", 100), new Categorie("travel", "6600", "Déplacements", 100), new Categorie("meals", "6610", "Repas et représentation", 50), new Categorie("vehicle", "6620", "Véhicule", 100),
            new Categorie("training", "6700", "Formation", 100), new Categorie("equipment", "6800", "Équipement", 100), new Categorie("homeoffice", "6810", "Bureau à domicile", 100), new Categorie("other", "6900", "Autres dépenses", 100),
            new Categorie("uncategorized", "6999", "Dépense non catégorisée", 100));

    /** Les catégories de la saisie rapide ({@code POST /expenses}) et leur déductibilité. */
    public static final Map<String, Double> DEDUCTIBILITE_PAR_DEFAUT = Map.of("office", 100.0, "travel", 100.0, "meals", 50.0, "software", 100.0, "training", 100.0, "telecom", 100.0, "vehicle", 100.0, "other", 100.0);

    public record Ligne(String category, String description, double amountHT, double gst, double qst, double deductiblePct) {
    }

    public record Depense(String id, String date, String category, String description, double amountHT, double gst, double qst, double deductiblePct, String employeeId, String tenantId, String payee,
                          String paymentAccount, String paymentMethod, String refNumber, String memo, List<String> attachments, List<Ligne> lines, String glEntryId) {
        /** Les lignes à agréger : celles du document, ou les champs plats comme une seule ligne. */
        public List<Ligne> lignesEffectives() {
            return lines != null && !lines.isEmpty() ? lines : List.of(new Ligne(category, description, amountHT, gst, qst, deductiblePct));
        }

        public Depense avecEcriture(String entryId) {
            return new Depense(id, date, category, description, amountHT, gst, qst, deductiblePct, employeeId, tenantId, payee, paymentAccount, paymentMethod, refNumber, memo, attachments, lines, entryId);
        }
    }

    public record PieceJointe(String id, String expenseId, String filename, String mimeType, long size, String createdAt) {
    }

    public record ContenuPieceJointe(PieceJointe meta, String contentB64) {
    }

    public record Taxes(double gst, double qst, double ttc) {
    }

    public record TotauxCategorie(int count, double totalHT, double deductible) {
    }

    public record Sommaire(double totalHT, double totalGST, double totalQST, double totalTTC, double deductibleExpense, double recoverableGST, double recoverableQST, double estimatedTaxSaving,
                           Map<String, TotauxCategorie> byCategory) {
    }

    /** Une ligne d'écriture à passer au grand livre : le compte est désigné par son code, créé au besoin. */
    public record LigneGrandLivre(String code, String nom, String type, double debit, double credit) {
    }

    public record EcritureDepense(String date, String memo, List<LigneGrandLivre> lignes) {
    }

    public interface Depot {
        List<Depense> lister(String tenantId);

        Optional<Depense> lire(String tenantId, String id);

        void ajouter(Depense d);

        void mettreAJour(Depense d);

        boolean supprimer(String id);
    }

    public interface PiecesJointes {
        List<PieceJointe> lister(String tenantId, String expenseId);

        Optional<ContenuPieceJointe> piece(String tenantId, String id);

        void ajouter(String tenantId, ContenuPieceJointe p);

        boolean supprimer(String tenantId, String id);

        void supprimerDeLaDepense(String tenantId, String expenseId);
    }

    /** Port : le grand livre, tenu par la comptabilité — l'écriture posée est comptabilisée d'emblée. */
    public interface GrandLivre {
        String comptabiliser(String tenantId, EcritureDepense e);

        void retirer(String tenantId, String entryId);
    }

    public static double r2(double n) {
        return Math.round(n * 100) / 100.0;
    }

    public static Taxes taxesDeHT(double montantHT) {
        return new Taxes(r2(montantHT * TAUX_TPS), r2(montantHT * TAUX_TVQ), r2(montantHT * (1 + TAUX_TPS + TAUX_TVQ)));
    }

    public static Optional<Categorie> categorie(String key) {
        return CATEGORIES.stream().filter(c -> c.key().equals(key)).findFirst();
    }

    public static double deductibiliteParDefaut(String key) {
        return categorie(key).map(Categorie::deductiblePct).orElseGet(() -> DEDUCTIBILITE_PAR_DEFAUT.getOrDefault(key, 100.0));
    }

    /** {@code summarizeExpenses} : totaux, part déductible, taxes récupérables, économie estimée au taux marginal. */
    public static Sommaire sommaire(List<Depense> depenses, double tauxMarginal) {
        double ht = 0;
        double tps = 0;
        double tvq = 0;
        double deductible = 0;
        double tpsRecuperable = 0;
        double tvqRecuperable = 0;
        Map<String, double[]> parCategorie = new LinkedHashMap<>();
        for (Depense d : depenses) {
            for (Ligne l : d.lignesEffectives()) {
                double pct = l.deductiblePct() / 100;
                ht += l.amountHT();
                tps += l.gst();
                tvq += l.qst();
                deductible += l.amountHT() * pct;
                tpsRecuperable += l.gst() * pct;
                tvqRecuperable += l.qst() * pct;
                double[] cat = parCategorie.computeIfAbsent(l.category(), k -> new double[3]);
                cat[0]++;
                cat[1] += l.amountHT();
                cat[2] += l.amountHT() * pct;
            }
        }
        Map<String, TotauxCategorie> categories = new LinkedHashMap<>();
        parCategorie.forEach((k, v) -> categories.put(k, new TotauxCategorie((int) v[0], r2(v[1]), r2(v[2]))));
        return new Sommaire(r2(ht), r2(tps), r2(tvq), r2(ht + tps + tvq), r2(deductible), r2(tpsRecuperable), r2(tvqRecuperable), r2(deductible * tauxMarginal), categories);
    }

    /** Une saisie rapide validée, prête à devenir une dépense une fois son dossier connu. */
    public record SaisieRapide(String date, String category, String description, double amountHT, double gst, double qst, double deductiblePct) {
        public Depense vers(String id, String tenantId, String employeeId) {
            return new Depense(id, date, category, description, amountHT, gst, qst, deductiblePct, employeeId, tenantId, null, null, null, null, null, null, null, null);
        }
    }

    /** {@code POST /expenses} : la saisie rapide à une ligne ; les taxes données ensemble, sinon calculées. */
    public static SaisieRapide saisieRapide(Map<String, Object> c) {
        if (!(c.get("date") instanceof String date) || date.isEmpty() || !(c.get("category") instanceof String category) || category.isEmpty()
                || !(c.get("description") instanceof String description) || description.isEmpty() || !(c.get("amountHT") instanceof Number montant)) {
            throw new IllegalArgumentException("date, category, description, amountHT required");
        }
        if (!DEDUCTIBILITE_PAR_DEFAUT.containsKey(category)) {
            throw new IllegalArgumentException("unknown category: " + category);
        }
        Taxes taxes = c.get("gst") instanceof Number g && c.get("qst") instanceof Number q ? new Taxes(g.doubleValue(), q.doubleValue(), 0) : taxesDeHT(montant.doubleValue());
        double pct = c.get("deductiblePct") instanceof Number p ? p.doubleValue() : DEDUCTIBILITE_PAR_DEFAUT.get(category);
        return new SaisieRapide(date, category, description, montant.doubleValue(), taxes.gst(), taxes.qst(), pct);
    }

    private static Ligne ligne(Map<?, ?> l) {
        double montant = ((Number) l.get("amountHT")).doubleValue();
        Taxes taxes = l.get("gst") instanceof Number g && l.get("qst") instanceof Number q ? new Taxes(r2(g.doubleValue()), r2(q.doubleValue()), 0) : taxesDeHT(montant);
        String category = String.valueOf(l.get("category"));
        return new Ligne(category, l.get("description") == null ? "" : String.valueOf(l.get("description")), r2(montant), r2(taxes.gst()), r2(taxes.qst()),
                l.get("deductiblePct") instanceof Number p ? p.doubleValue() : deductibiliteParDefaut(category));
    }

    private static boolean categorieVide(Map<?, ?> l) {
        return !(l.get("category") instanceof String s) || s.isEmpty();
    }

    private static boolean montantInvalide(Map<?, ?> l) {
        return !(l.get("amountHT") instanceof Number n) || n.doubleValue() <= 0;
    }

    private static List<Map<?, ?>> lignesBrutes(Object v) {
        List<Map<?, ?>> lignes = new ArrayList<>();
        if (v instanceof List<?> liste) {
            for (Object o : liste) {
                lignes.add(o instanceof Map<?, ?> m ? m : Map.of());
            }
        }
        return lignes;
    }

    /** {@code createExpenseDocument} : la date, au moins une ligne, chaque ligne catégorisée et positive — les refus dits ensemble. */
    public static Depense document(String id, String tenantId, Map<String, Object> c, String employeeId) {
        List<String> erreurs = new ArrayList<>();
        if (!(c.get("date") instanceof String date) || !DATE.matcher(date).matches()) {
            erreurs.add("date must be YYYY-MM-DD");
        }
        List<Map<?, ?>> brutes = lignesBrutes(c.get("lines"));
        if (brutes.isEmpty()) {
            erreurs.add("at least one line required");
        }
        for (Map<?, ?> l : brutes) {
            if (categorieVide(l)) {
                erreurs.add("line category required");
            }
            if (montantInvalide(l)) {
                erreurs.add("line amountHT must be > 0");
            }
        }
        if (!erreurs.isEmpty()) {
            throw new IllegalArgumentException(String.join("; ", erreurs));
        }
        List<Ligne> lignes = brutes.stream().map(Depenses::ligne).toList();
        String memo = texte(c.get("memo"));
        Depense base = new Depense(id, (String) c.get("date"), lignes.get(0).category(), memo != null && !memo.isEmpty() ? memo : lignes.get(0).description(), 0, 0, 0, 0, employeeId, tenantId, texte(c.get("payee")),
                texte(c.get("paymentAccount")), texte(c.get("paymentMethod")), texte(c.get("refNumber")), memo, nomsDePieces(c.get("attachments")), lignes, null);
        return totaliser(base, lignes);
    }

    private static Depense totaliser(Depense d, List<Ligne> lignes) {
        double ht = r2(lignes.stream().mapToDouble(Ligne::amountHT).sum());
        double tps = r2(lignes.stream().mapToDouble(Ligne::gst).sum());
        double tvq = r2(lignes.stream().mapToDouble(Ligne::qst).sum());
        double pct = ht > 0 ? r2(lignes.stream().mapToDouble(l -> l.amountHT() * l.deductiblePct()).sum() / ht) : 100;
        return new Depense(d.id(), d.date(), lignes.get(0).category(), d.description(), ht, tps, tvq, pct, d.employeeId(), d.tenantId(), d.payee(), d.paymentAccount(), d.paymentMethod(), d.refNumber(), d.memo(), d.attachments(), lignes, d.glEntryId());
    }

    /** {@code updateExpenseDocument} : chaque clé présente remplace, même par un vide ; les lignes, si données, sont revalidées une à une. */
    public static Depense corriger(Depense e, Map<String, Object> c) {
        String date = e.date();
        if (c.containsKey("date")) {
            if (!(c.get("date") instanceof String d) || !DATE.matcher(d).matches()) {
                throw new IllegalArgumentException("date must be YYYY-MM-DD");
            }
            date = d;
        }
        String memo = c.containsKey("memo") ? texte(c.get("memo")) : e.memo();
        String description = c.containsKey("memo") && memo != null && !memo.isEmpty() ? memo : e.description();
        List<String> pieces = c.get("attachments") instanceof List<?> ? nomsDePieces(c.get("attachments")) : e.attachments();
        Depense base = new Depense(e.id(), date, e.category(), description, e.amountHT(), e.gst(), e.qst(), e.deductiblePct(), e.employeeId(), e.tenantId(), c.containsKey("payee") ? texte(c.get("payee")) : e.payee(),
                c.containsKey("paymentAccount") ? texte(c.get("paymentAccount")) : e.paymentAccount(), c.containsKey("paymentMethod") ? texte(c.get("paymentMethod")) : e.paymentMethod(),
                c.containsKey("refNumber") ? texte(c.get("refNumber")) : e.refNumber(), memo, pieces, e.lines(), e.glEntryId());
        if (!(c.get("lines") instanceof List<?>)) {
            return base;
        }
        List<Map<?, ?>> brutes = lignesBrutes(c.get("lines"));
        if (brutes.isEmpty()) {
            throw new IllegalArgumentException("at least one line required");
        }
        for (Map<?, ?> l : brutes) {
            if (categorieVide(l)) {
                throw new IllegalArgumentException("line category required");
            }
            if (montantInvalide(l)) {
                throw new IllegalArgumentException("line amountHT must be > 0");
            }
        }
        return totaliser(base, brutes.stream().map(Depenses::ligne).toList());
    }

    /** {@code postExpenseToLedger} : débit des charges par catégorie et des taxes à recouvrer, crédit du compte de paiement, TTC. */
    public static EcritureDepense ecriture(Depense e) {
        List<LigneGrandLivre> lignes = new ArrayList<>();
        double tps = 0;
        double tvq = 0;
        double ttc = 0;
        for (Ligne l : e.lignesEffectives()) {
            Categorie def = categorie(l.category()).orElse(new Categorie(l.category(), "6999", "Dépense non catégorisée", 100));
            lignes.add(new LigneGrandLivre(def.code(), def.label(), "expense", r2(l.amountHT()), 0));
            tps = r2(tps + l.gst());
            tvq = r2(tvq + l.qst());
            ttc = r2(ttc + l.amountHT() + l.gst() + l.qst());
        }
        if (tps > 0) {
            lignes.add(new LigneGrandLivre("1185", "TPS à recouvrer (CTI)", "asset", tps, 0));
        }
        if (tvq > 0) {
            lignes.add(new LigneGrandLivre("1186", "TVQ à recouvrer (RTI)", "asset", tvq, 0));
        }
        String compte = e.paymentAccount() == null || e.paymentAccount().trim().isEmpty() ? "Encaisse" : e.paymentAccount().trim();
        lignes.add(new LigneGrandLivre("1000", compte, "asset", 0, ttc));
        return new EcritureDepense(e.date(), "Dépense " + (e.refNumber() != null ? e.refNumber() : e.id()) + (e.payee() != null ? " — " + e.payee() : ""), lignes);
    }

    /** {@code addAttachment} : un nom, un contenu (le préfixe {@code data:} toléré), au plus 50 Mo — tout format. */
    public static ContenuPieceJointe pieceJointe(String id, String expenseId, Map<String, Object> c, String instant) {
        String nom = c.get("filename") == null ? "" : String.valueOf(c.get("filename")).trim();
        if (nom.isEmpty()) {
            throw new IllegalArgumentException("filename required");
        }
        String b64 = c.get("dataBase64") == null ? "" : String.valueOf(c.get("dataBase64")).replaceFirst("^data:[^;]+;base64,", "");
        if (b64.isEmpty()) {
            throw new IllegalArgumentException("attachment content required");
        }
        long taille = (long) b64.length() * 3 / 4;
        if (taille > TAILLE_MAX) {
            throw new IllegalArgumentException("attachment exceeds 50 MB limit");
        }
        return new ContenuPieceJointe(new PieceJointe(id, expenseId, nom, texte(c.get("mimeType")), taille, instant), b64);
    }

    /** La recherche du tableau : bénéficiaire, référence, date, mémo et catégorie, sans la casse. */
    public static boolean correspond(Depense e, String recherche) {
        if (recherche == null || recherche.isEmpty()) {
            return true;
        }
        String texte = (vide(e.payee()) + " " + vide(e.refNumber()) + " " + e.date() + " " + vide(e.memo()) + " " + e.category()).toLowerCase();
        return texte.contains(recherche.toLowerCase());
    }

    private static String vide(String s) {
        return s == null ? "" : s;
    }

    private static String texte(Object v) {
        return v == null ? null : String.valueOf(v);
    }

    private static List<String> nomsDePieces(Object v) {
        if (!(v instanceof List<?> l)) {
            return null;
        }
        return l.stream().map(String::valueOf).toList();
    }
}
