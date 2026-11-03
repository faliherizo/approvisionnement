package qc.levis.contrat.fiscalite.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <strong>Le domaine fiscal — TPS, TVQ et TVH</strong> — port fidèle de
 * {@code backend/src/fiscalite/domaine.ts} (STO DS2026-120-03, FIN181 à FIN184).
 *
 * <p>L'onglet « Gestion de la fiscalité » ne compte que <strong>quatre exigences, toutes
 * indispensables</strong>. Notées en tout ou rien, elles valent aujourd'hui zéro.
 *
 * <h2>La chose qui décide de tout : la STO est un organisme à activités <em>exonérées</em></h2>
 *
 * <p>Le transport en commun est <strong>exonéré</strong> de TPS et de TVQ. Ce n'est pas « détaxé
 * à 0 % », et la différence est l'inverse d'un détail :
 *
 * <table>
 *   <caption>Ce que chaque nature ouvre</caption>
 *   <tr><th>Nature</th><th>Taxe facturée</th><th>Crédit sur les intrants</th></tr>
 *   <tr><td>Taxable</td><td>oui</td><td><strong>plein</strong></td></tr>
 *   <tr><td>Détaxée (0 %)</td><td>non, mais la fourniture reste taxable</td><td><strong>plein</strong></td></tr>
 *   <tr><td>Exonérée</td><td>non</td><td><strong>aucun</strong></td></tr>
 * </table>
 *
 * <p>Un organisme dont l'activité principale est exonérée ne récupère donc pas la taxe sur ses
 * intrants par le mécanisme ordinaire : il obtient un remboursement <em>partiel</em>, à un taux
 * fixé par règlement. D'où FIN181, qui demande que ce taux soit <em>configurable</em> — jamais
 * écrit en dur, parce qu'il change et qu'il diffère d'un organisme à l'autre.
 *
 * <p>Confondre exonéré et détaxé fait récupérer 100 % là où l'on a droit à une fraction. L'écart
 * n'apparaît dans aucun état financier : il apparaît dans un avis de cotisation, avec intérêts.
 *
 * <p>Les montants sont en {@link BigDecimal} : une déclaration de taxes se compare au cent près
 * avec celle de Revenu Québec, et {@code double} fabrique les écarts qu'on passe une journée à
 * chercher.
 */
public final class Fiscalite {

    private static final int CENTS = 2;
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(CENTS, RoundingMode.HALF_UP);
    private static final BigDecimal CENT = BigDecimal.valueOf(100);

    /** La limite légale sur les repas et le divertissement : 50 %, et ce n'est pas configurable. */
    public static final BigDecimal PART_ADMISSIBLE_REPAS = new BigDecimal("0.5");

    private Fiscalite() {
    }

    /** Erreur métier du domaine — jamais un code de retour, jamais un {@code null} silencieux. */
    public static class FiscaliteInvalideException extends RuntimeException {
        public FiscaliteInvalideException(String message) {
            super(message);
        }
    }

    // ── Les ensembles fermés ────────────────────────────────────────────────

    /**
     * La juridiction qui décide des taxes applicables (FIN182).
     *
     * <p>Le découpage suit le <strong>régime</strong>, pas la province : l'Ontario et la
     * Nouvelle-Écosse appliquent toutes deux la TVH, à des taux différents.
     */
    public enum Regime {
        QC("qc"), TVH("tvh"), TPS_SEULE("tps_seule"), ETATS_UNIS("etats_unis"), HORS_AMERIQUE("hors_amerique");

        private final String code;

        Regime(String code) {
            this.code = code;
        }

        public String code() {
            return code;
        }

        public static Regime de(String code) {
            return Arrays.stream(values()).filter(v -> v.code.equals(code)).findFirst()
                    .orElseThrow(() -> new FiscaliteInvalideException(
                            "régime fiscal inconnu : « " + code + " » (attendu : " + codes() + ")"));
        }

        public static String codes() {
            return String.join(", ", Arrays.stream(values()).map(Regime::code).toList());
        }
    }

    /** Ce qu'une fourniture est, du point de vue de la taxe. La distinction porte tout le domaine. */
    public enum Nature {
        TAXABLE("taxable"), DETAXEE("detaxee"), EXONEREE("exoneree");

        private final String code;

        Nature(String code) {
            this.code = code;
        }

        public String code() {
            return code;
        }

        public static Nature de(String code) {
            return Arrays.stream(values()).filter(v -> v.code.equals(code)).findFirst()
                    .orElseThrow(() -> new FiscaliteInvalideException(
                            "nature de fourniture inconnue : « " + code + " » (attendu : " + codes() + ")"));
        }

        public static String codes() {
            return String.join(", ", Arrays.stream(values()).map(Nature::code).toList());
        }
    }

    /** Le sens de l'opération : on perçoit sur une vente, on récupère sur un achat. */
    public enum Sens {
        ACHAT("achat"), VENTE("vente");

        private final String code;

        Sens(String code) {
            this.code = code;
        }

        public String code() {
            return code;
        }

        public static Sens de(String code) {
            return Arrays.stream(values()).filter(v -> v.code.equals(code)).findFirst()
                    .orElseThrow(() -> new FiscaliteInvalideException(
                            "sens d'opération inconnu : « " + code + " » (attendu : achat, vente)"));
        }
    }

    /**
     * Le statut fiscal du fournisseur (FIN183).
     *
     * <p>{@code PETIT_FOURNISSEUR} n'est pas une commodité : sous 30 000 $ de fournitures
     * taxables, un fournisseur n'a pas à s'inscrire, ne facture pas de taxe, et celle qu'il
     * facturerait quand même ne donne droit à <strong>aucun</strong> crédit.
     */
    public enum StatutFournisseur {
        INSCRIT("inscrit"), PETIT_FOURNISSEUR("petit_fournisseur"), ETRANGER("etranger");

        private final String code;

        StatutFournisseur(String code) {
            this.code = code;
        }

        public String code() {
            return code;
        }

        public static StatutFournisseur de(String code) {
            return Arrays.stream(values()).filter(v -> v.code.equals(code)).findFirst()
                    .orElseThrow(() -> new FiscaliteInvalideException(
                            "statut de fournisseur inconnu : « " + code
                                    + " » (attendu : inscrit, petit_fournisseur, etranger)"));
        }
    }

    /**
     * La catégorie de dépense, pour les restrictions légales.
     *
     * <p>{@code REPAS_DIVERTISSEMENT} porte la limite de 50 % ; {@code TITRE_TRANSPORT} porte
     * l'exonération propre au transport en commun.
     */
    public enum CategorieDepense {
        COURANTE("courante"), REPAS_DIVERTISSEMENT("repas_divertissement"),
        TITRE_TRANSPORT("titre_transport"), IMMOBILISATION("immobilisation");

        private final String code;

        CategorieDepense(String code) {
            this.code = code;
        }

        public String code() {
            return code;
        }

        public static CategorieDepense de(String code) {
            return Arrays.stream(values()).filter(v -> v.code.equals(code)).findFirst()
                    .orElseThrow(() -> new FiscaliteInvalideException(
                            "catégorie de dépense inconnue : « " + code + " » (attendu : " + codes() + ")"));
        }

        public static String codes() {
            return String.join(", ", Arrays.stream(values()).map(CategorieDepense::code).toList());
        }
    }

    private static BigDecimal cents(BigDecimal n) {
        return n.setScale(CENTS, RoundingMode.HALF_UP);
    }

    // ── Le paramétrage d'un organisme (FIN181, FIN182) ──────────────────────

    /**
     * Un taux, en pourcentage.
     *
     * @param recuperation la part récupérable <strong>par cet organisme</strong> (FIN181) : 100
     *                     pour un organisme à activités taxables, une fraction pour un organisme
     *                     de transport en commun. C'est le nombre que l'exigence demande de
     *                     configurer, et il n'est écrit nulle part dans le code.
     * @param autocotisee  vrai quand le fournisseur ne facture pas la taxe et que l'acheteur la
     *                     déclare lui-même (FIN183, FIN184).
     */
    public record TauxTaxe(String code, BigDecimal taux, BigDecimal recuperation, boolean autocotisee) { }

    public record ParametrageFiscal(Regime regime, List<TauxTaxe> taux) { }

    public record TauxStatutaire(String code, BigDecimal taux) { }

    /**
     * Le barème statutaire d'un régime : public et rarement changeant.
     *
     * <p>La <strong>récupération</strong>, elle, est propre à l'organisme et doit être fournie :
     * un défaut de 100 % paraîtrait innocent et ferait réclamer à un organisme exonéré un crédit
     * auquel il n'a pas droit.
     */
    public static List<TauxStatutaire> baremeStatutaire(Regime regime) {
        return switch (regime) {
            case QC -> List.of(new TauxStatutaire("TPS", new BigDecimal("5")),
                    new TauxStatutaire("TVQ", new BigDecimal("9.975")));
            // Le taux de TVH varie de 13 % à 15 % selon la province : il est **fourni**, pas deviné.
            case TVH -> List.of(new TauxStatutaire("TVH", BigDecimal.ZERO));
            case TPS_SEULE -> List.of(new TauxStatutaire("TPS", new BigDecimal("5")));
            // Un fournisseur étranger ne facture ni TPS ni TVQ : l'acheteur s'autocotise (FIN184).
            case ETATS_UNIS, HORS_AMERIQUE -> List.of();
        };
    }

    // ── Le calcul (FIN181 à FIN184) ─────────────────────────────────────────

    public record Operation(Sens sens, BigDecimal base, Nature nature, CategorieDepense categorie,
                            StatutFournisseur statutFournisseur, BigDecimal taxeFacturee) { }

    public record LigneTaxe(String code, BigDecimal taux, BigDecimal montant,
                            BigDecimal recuperable, BigDecimal nonRecuperable, boolean autocotisee) { }

    public record ResultatFiscal(BigDecimal base, BigDecimal baseAdmissible, List<LigneTaxe> lignes,
                                 BigDecimal totalTaxe, BigDecimal totalRecuperable,
                                 BigDecimal totalNonRecuperable, BigDecimal totalAutocotise,
                                 BigDecimal coutTotal, List<String> motifs) { }

    private static BigDecimal exigerMontant(BigDecimal v, String champ) {
        if (v == null || v.signum() < 0) {
            throw new FiscaliteInvalideException("« " + champ + " » doit être un montant positif ou nul");
        }
        return v;
    }

    private static BigDecimal exigerTaux(BigDecimal v, String champ) {
        if (v == null || v.signum() < 0 || v.compareTo(CENT) > 0) {
            throw new FiscaliteInvalideException("« " + champ + " » doit être un pourcentage entre 0 et 100");
        }
        return v;
    }

    /**
     * <strong>Le calcul des taxes d'une opération</strong>, et de ce qui en est récupérable.
     *
     * <p>L'ordre des décisions n'est pas arbitraire — chacune annule la suivante :
     *
     * <ol>
     *   <li>Le fournisseur est-il <strong>en droit</strong> de facturer une taxe ?</li>
     *   <li>La fourniture est-elle <strong>exonérée</strong> ? Alors aucune taxe, aucun crédit.</li>
     *   <li>La base est-elle <strong>restreinte</strong> ? Les repas comptent pour la moitié.</li>
     *   <li>Enfin seulement : le taux statutaire, puis le taux de récupération de l'organisme.</li>
     * </ol>
     *
     * <p>Intervertir 2 et 3 ferait calculer une restriction sur une base exonérée — un zéro
     * savamment divisé par deux.
     */
    public static ResultatFiscal calculer(Operation operation, ParametrageFiscal parametrage) {
        BigDecimal base = exigerMontant(operation.base(), "base");
        List<String> motifs = new ArrayList<>();

        // 1. Un petit fournisseur ne facture pas de taxe, et celle qu'il facturerait quand même
        //    est refusée à la vérification : la réclamer serait la payer deux fois.
        if (operation.statutFournisseur() == StatutFournisseur.PETIT_FOURNISSEUR) {
            if (operation.taxeFacturee() != null && operation.taxeFacturee().signum() > 0) {
                throw new FiscaliteInvalideException("un petit fournisseur ne facture pas de taxe : les "
                        + texte(cents(operation.taxeFacturee()))
                        + " $ portés à la facture ne donnent droit à aucun crédit");
            }
            return vide(base, List.of("fournisseur non inscrit (petit fournisseur) : aucune taxe, aucun crédit"));
        }

        // 2. Exonéré : ni taxe perçue, ni crédit sur les intrants.
        if (operation.nature() == Nature.EXONEREE) {
            return vide(base, List.of("fourniture exonérée : aucune taxe n'est perçue et aucun crédit n'est ouvert"));
        }
        if (operation.categorie() == CategorieDepense.TITRE_TRANSPORT) {
            return vide(base, List.of("titre de transport en commun : fourniture exonérée par la loi"));
        }

        // 3. La restriction légale s'applique à la **base**, avant tout taux.
        BigDecimal baseAdmissible = base;
        if (operation.categorie() == CategorieDepense.REPAS_DIVERTISSEMENT) {
            baseAdmissible = cents(base.multiply(PART_ADMISSIBLE_REPAS));
            motifs.add("repas et divertissement : base admissible limitée à 50 % ("
                    + texte(baseAdmissible) + " sur " + texte(cents(base)) + ")");
        }

        // 4. Détaxé : la fourniture reste taxable, au taux zéro ; le crédit demeure.
        boolean detaxee = operation.nature() == Nature.DETAXEE;
        if (detaxee) {
            motifs.add("fourniture détaxée : taxe au taux zéro, mais le crédit sur les intrants demeure");
        }

        List<LigneTaxe> lignes = new ArrayList<>();
        for (TauxTaxe t : parametrage.taux()) {
            BigDecimal taux = detaxee ? BigDecimal.ZERO : exigerTaux(t.taux(), "taux " + t.code());
            BigDecimal recuperation = exigerTaux(t.recuperation(), "récupération " + t.code());
            BigDecimal montant = cents(baseAdmissible.multiply(taux).divide(CENT, 10, RoundingMode.HALF_UP));
            // Sur une **vente**, il n'y a rien à récupérer : la taxe perçue est due à l'État.
            BigDecimal recuperable = operation.sens() == Sens.ACHAT
                    ? cents(montant.multiply(recuperation).divide(CENT, 10, RoundingMode.HALF_UP)) : ZERO;
            if (operation.sens() == Sens.ACHAT && recuperation.compareTo(CENT) < 0) {
                motifs.add(t.code() + " récupérable à " + texte(recuperation) + " % : "
                        + texte(cents(montant.subtract(recuperable))) + " reste à la charge de l'organisme");
            }
            if (t.autocotisee()) {
                motifs.add(t.code() + " autocotisée : le fournisseur ne l'a pas facturée, l'organisme la déclare lui-même");
            }
            lignes.add(new LigneTaxe(t.code(), taux, montant, recuperable,
                    cents(montant.subtract(recuperable)), t.autocotisee()));
        }

        BigDecimal totalTaxe = somme(lignes, LigneTaxe::montant);
        BigDecimal totalRecuperable = somme(lignes, LigneTaxe::recuperable);
        BigDecimal totalNonRecuperable = somme(lignes, LigneTaxe::nonRecuperable);
        BigDecimal totalAutocotise = cents(lignes.stream()
                .map(l -> l.autocotisee() ? l.montant() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        return new ResultatFiscal(cents(base), baseAdmissible, List.copyOf(lignes), totalTaxe,
                totalRecuperable, totalNonRecuperable, totalAutocotise,
                // **La taxe non récupérable est un coût** : c'est la ligne qui empêche de gonfler
                // une créance qui ne sera jamais encaissée.
                cents(base.add(totalNonRecuperable)), List.copyOf(motifs));
    }

    /**
     * La somme d'un champ monétaire, <b>un montant absent valant zéro</b>.
     *
     * <p>Et non « rien » : c'est ce que l'adaptateur écrit au grand livre, et une somme doit lire
     * la même chose que l'écriture. Sans cette normalisation, une ligne au seul crédit levait un
     * {@code NullPointerException} — un corps incomplet devenu 500, ce que la norme interdit.
     */
    private static <T> BigDecimal somme(List<T> lignes, java.util.function.Function<T, BigDecimal> f) {
        return cents(lignes.stream().map(f).map(v -> v == null ? ZERO : v)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    private static ResultatFiscal vide(BigDecimal base, List<String> motifs) {
        return new ResultatFiscal(cents(base), ZERO, List.of(), ZERO, ZERO, ZERO, ZERO, cents(base), motifs);
    }

    /** Les montants dans les messages s'écrivent comme côté Node : {@code 5000}, pas {@code 5000.00}. */
    private static String texte(BigDecimal montant) {
        return montant.stripTrailingZeros().toPlainString();
    }

    // ── Les achats à l'étranger (FIN184) ────────────────────────────────────

    public record DemandeEtranger(Regime regime, boolean bienImporte, BigDecimal recuperationTps,
                                  BigDecimal recuperationTvq, BigDecimal tauxTvh) { }

    /**
     * Le paramétrage d'un <strong>achat hors Québec ou hors Canada</strong>.
     *
     * <ul>
     *   <li><strong>Autre province</strong> : le fournisseur facture la TPS, pas la TVQ. L'acheteur
     *       québécois s'autocotise la TVQ (FIN183).</li>
     *   <li><strong>États-Unis</strong> : aucun des deux. Sur un <em>bien</em> importé, la TPS est
     *       perçue à la frontière ; sur un <em>service</em>, les deux s'autocotisent (FIN184).</li>
     *   <li><strong>Hors Amérique</strong> : même traitement — la distinction est douanière, pas
     *       fiscale.</li>
     * </ul>
     *
     * <p>Confondre un bien et un service fait autocotiser une TPS déjà payée à la douane : donc la
     * payer deux fois, et réclamer un crédit qui sera refusé faute de pièce.
     */
    public static ParametrageFiscal parametrageAchatEtranger(DemandeEtranger d) {
        BigDecimal tps = new BigDecimal("5");
        BigDecimal tvq = new BigDecimal("9.975");
        return switch (d.regime()) {
            // Rien à autocotiser : le fournisseur québécois facture les deux.
            case QC -> new ParametrageFiscal(d.regime(), List.of(
                    new TauxTaxe("TPS", tps, d.recuperationTps(), false),
                    new TauxTaxe("TVQ", tvq, d.recuperationTvq(), false)));
            case TVH -> {
                if (d.tauxTvh() == null) {
                    throw new FiscaliteInvalideException(
                            "le taux de TVH varie de 13 % à 15 % selon la province : il doit être fourni");
                }
                // La TVH remplace TPS et TVQ ; il n'y a **rien** à autocotiser, contrairement à
                // l'intuition — la part provinciale de la TVH n'est pas de la TVQ.
                yield new ParametrageFiscal(d.regime(), List.of(
                        new TauxTaxe("TVH", exigerTaux(d.tauxTvh(), "taux TVH"), d.recuperationTps(), false)));
            }
            // Le fournisseur facture la TPS ; la TVQ, elle, s'autocotise.
            case TPS_SEULE -> new ParametrageFiscal(d.regime(), List.of(
                    new TauxTaxe("TPS", tps, d.recuperationTps(), false),
                    new TauxTaxe("TVQ", tvq, d.recuperationTvq(), true)));
            case ETATS_UNIS, HORS_AMERIQUE -> new ParametrageFiscal(d.regime(), List.of(
                    // Sur un bien importé, la douane a déjà perçu la TPS : la réautocotiser la
                    // ferait payer deux fois.
                    new TauxTaxe("TPS", tps, d.recuperationTps(), !d.bienImporte()),
                    new TauxTaxe("TVQ", tvq, d.recuperationTvq(), true)));
        };
    }

    // ── La ventilation comptable (FIN181) ───────────────────────────────────

    public record LigneVentilation(String compte, BigDecimal debit, BigDecimal credit, String libelle) { }

    public record ComptesFiscaux(String taxeARecevoir, String taxeAPayer, String charge, String contrepartie) { }

    /**
     * La ventilation d'un achat, taxes comprises.
     *
     * <p>C'est ici que la règle « la taxe non récupérable est un coût » devient une écriture, et
     * non une intention.
     */
    public static List<LigneVentilation> ventiler(ResultatFiscal r, ComptesFiscaux comptes) {
        List<LigneVentilation> lignes = new ArrayList<>();
        // La dépense porte la base **entière** — la restriction des repas limite le *crédit*, pas
        // le coût : le repas a bien été payé en totalité.
        lignes.add(new LigneVentilation(comptes.charge(), cents(r.base().add(r.totalNonRecuperable())), ZERO,
                r.totalNonRecuperable().signum() > 0 ? "dépense, taxe non récupérable incluse" : "dépense"));
        if (r.totalRecuperable().signum() > 0) {
            lignes.add(new LigneVentilation(comptes.taxeARecevoir(), r.totalRecuperable(), ZERO,
                    "taxe récupérable auprès de l'État"));
        }
        if (r.totalAutocotise().signum() > 0) {
            lignes.add(new LigneVentilation(comptes.taxeAPayer(), ZERO, r.totalAutocotise(),
                    "taxe autocotisée, due à l'État"));
        }
        // Le fournisseur reçoit la base plus la taxe qu'il a **facturée** — jamais celle qu'on
        // s'autocotise, qui ne lui a pas été versée.
        BigDecimal facturee = cents(r.totalTaxe().subtract(r.totalAutocotise()));
        lignes.add(new LigneVentilation(comptes.contrepartie(), ZERO, cents(r.base().add(facturee)),
                "somme due au fournisseur"));
        return List.copyOf(lignes);
    }

    /**
     * Une ventilation fausse se voit ici, pas au bilan.
     *
     * <p>Un montant absent vaut zéro : voir {@link #somme}.
     */
    public static boolean equilibree(List<LigneVentilation> lignes) {
        return somme(lignes, LigneVentilation::debit).compareTo(somme(lignes, LigneVentilation::credit)) == 0;
    }
}
