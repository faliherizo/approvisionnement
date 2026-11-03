package qc.levis.contrat.clm.domain;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import qc.levis.contrat.regle.domain.ConditionDef;

/**
 * Modèle de contrat (ST-05, UI-07 à UI-09) : corps à variables {@code {{cle}}}, champs typés positionnés sur le
 * document et assignables à un rôle de participant, clauses conditionnelles. Règles pures, sans E/S.
 */
public final class Modele {

    public static final String MARQUEUR_CLAUSES = "clauses";
    public static final String ROLE_INITIATEUR = "INITIATEUR";
    private static final Pattern VARIABLE = Pattern.compile("\\{\\{\\s*([\\w.-]+)\\s*}}");
    private static final Pattern CLE = Pattern.compile("^[A-Za-z][\\w.-]{0,79}$");

    public enum Statut {
        BROUILLON, EN_REVISION, PUBLIE, RETIRE;

        public Statut vers(Statut cible) {
            Set<Statut> permis = switch (this) {
                case BROUILLON -> EnumSet.of(EN_REVISION);
                case EN_REVISION -> EnumSet.of(PUBLIE, BROUILLON);
                case PUBLIE -> EnumSet.of(RETIRE);
                case RETIRE -> EnumSet.noneOf(Statut.class);
            };
            if (!permis.contains(cible)) {
                throw new IllegalStateException("Transition de modèle interdite : " + this + " → " + cible);
            }
            return cible;
        }
    }

    public enum TypeChamp { TEXTE, LISTE, DATE, CASE, SIGNATURE }

    /** Position en pourcentage de la page, origine en haut à gauche. */
    public record Position(int page, double x, double y, double largeur, double hauteur) {
    }

    public record Champ(String cle, String libelle, TypeChamp type, boolean obligatoire, String format, List<String> options,
                        String source, String valeurDefaut, List<ConditionDef> conditions, String roleDestinataire,
                        Position position) {
        public Champ {
            options = options == null ? List.of() : List.copyOf(options);
            conditions = conditions == null ? List.of() : List.copyOf(conditions);
        }

        /** Complété par l'initiateur à la génération plutôt que par un participant en cours de parcours. */
        public boolean completeALaGeneration() {
            return type != TypeChamp.SIGNATURE && (roleDestinataire == null || roleDestinataire.isBlank()
                    || ROLE_INITIATEUR.equals(roleDestinataire));
        }
    }

    public record Clause(String cle, String titre, String texte, String combinateur, List<ConditionDef> conditions) {
        public Clause {
            conditions = conditions == null ? List.of() : List.copyOf(conditions);
        }
    }

    private Modele() {
    }

    public static List<String> valider(String corps, List<Champ> champs, List<Clause> clauses) {
        List<String> erreurs = new ArrayList<>();
        List<Champ> lesChamps = champs == null ? List.of() : champs;
        List<Clause> lesClauses = clauses == null ? List.of() : clauses;
        Set<String> cles = new HashSet<>();
        for (Champ c : lesChamps) {
            validerChamp(c, cles, erreurs);
        }
        for (Champ c : lesChamps) {
            for (ConditionDef cond : c.conditions()) {
                if (!cles.contains(cond.champ())) {
                    erreurs.add("Variable source inconnue dans la condition de « " + c.cle() + " » : " + cond.champ());
                }
            }
        }
        circulaires(lesChamps).forEach(cle -> erreurs.add("Condition circulaire sur le champ « " + cle + " »."));
        Set<String> clesClauses = new HashSet<>();
        for (Clause cl : lesClauses) {
            if (cl.cle() == null || !CLE.matcher(cl.cle()).matches() || !clesClauses.add(cl.cle())) {
                erreurs.add("Clé de clause invalide ou en doublon : " + cl.cle());
            }
            for (ConditionDef cond : cl.conditions()) {
                if (!cles.contains(cond.champ())) {
                    erreurs.add("Variable source inconnue dans la clause « " + cl.cle() + " » : " + cond.champ());
                }
            }
        }
        for (String v : variables(corps)) {
            if (!cles.contains(v) && !MARQUEUR_CLAUSES.equals(v)) {
                erreurs.add("Variable source inconnue dans le corps : " + v);
            }
        }
        return erreurs;
    }

    private static void validerChamp(Champ c, Set<String> cles, List<String> erreurs) {
        if (c.cle() == null || !CLE.matcher(c.cle()).matches()) {
            erreurs.add("Nom technique invalide : " + c.cle());
            return;
        }
        if (!cles.add(c.cle())) {
            erreurs.add("Nom technique en doublon : " + c.cle());
        }
        if (c.libelle() == null || c.libelle().isBlank()) {
            erreurs.add("Libellé requis pour « " + c.cle() + " ».");
        }
        if (c.type() == null) {
            erreurs.add("Type requis pour « " + c.cle() + " ».");
            return;
        }
        if (c.type() == TypeChamp.LISTE && c.options().isEmpty()) {
            erreurs.add("Une liste exige des options : « " + c.cle() + " ».");
        }
        if (c.type() == TypeChamp.SIGNATURE && (c.roleDestinataire() == null || c.roleDestinataire().isBlank() || c.position() == null)) {
            erreurs.add("Zone sans signataire : « " + c.cle() + " » exige un rôle et une position.");
        }
        Pattern format = compiler(c, erreurs);
        if (c.valeurDefaut() != null && !c.valeurDefaut().isBlank() && !defautValide(c, format)) {
            erreurs.add("Valeur par défaut invalide pour « " + c.cle() + " ».");
        }
        if (c.position() != null && !positionValide(c.position())) {
            erreurs.add("Position hors du document pour « " + c.cle() + " ».");
        }
    }

    private static Pattern compiler(Champ c, List<String> erreurs) {
        if (c.format() == null || c.format().isBlank()) {
            return null;
        }
        try {
            return Pattern.compile(c.format());
        } catch (PatternSyntaxException e) {
            erreurs.add("Format invalide pour « " + c.cle() + " ».");
            return null;
        }
    }

    private static boolean defautValide(Champ c, Pattern format) {
        String v = c.valeurDefaut();
        if (format != null && !format.matcher(v).matches()) {
            return false;
        }
        return switch (c.type()) {
            case LISTE -> c.options().contains(v);
            case DATE -> estDate(v);
            case CASE -> "true".equals(v) || "false".equals(v);
            default -> true;
        };
    }

    private static boolean estDate(String v) {
        try {
            LocalDate.parse(v);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    static boolean positionValide(Position p) {
        return p.page() >= 1 && p.x() >= 0 && p.y() >= 0 && p.largeur() > 0 && p.hauteur() > 0
                && p.x() + p.largeur() <= 100 && p.y() + p.hauteur() <= 100;
    }

    /** Champs pris dans un cycle de conditions d'affichage (A dépend de B qui dépend de A). */
    static Set<String> circulaires(List<Champ> champs) {
        Map<String, List<String>> graphe = new HashMap<>();
        for (Champ c : champs) {
            if (c.cle() != null) {
                graphe.put(c.cle(), c.conditions().stream().map(ConditionDef::champ).toList());
            }
        }
        Set<String> enCycle = new LinkedHashSet<>();
        for (String depart : graphe.keySet()) {
            if (atteint(depart, depart, graphe, new HashSet<>())) {
                enCycle.add(depart);
            }
        }
        return enCycle;
    }

    private static boolean atteint(String cible, String courant, Map<String, List<String>> graphe, Set<String> vus) {
        for (String suivant : graphe.getOrDefault(courant, List.of())) {
            if (suivant.equals(cible)) {
                return true;
            }
            if (vus.add(suivant) && atteint(cible, suivant, graphe, vus)) {
                return true;
            }
        }
        return false;
    }

    public static Set<String> variables(String corps) {
        Set<String> res = new LinkedHashSet<>();
        if (corps == null) {
            return res;
        }
        Matcher m = VARIABLE.matcher(corps);
        while (m.find()) {
            res.add(m.group(1));
        }
        return res;
    }

    /**
     * Texte du contrat : variables remplacées, clauses retenues insérées au marqueur {@code {{clauses}}} ou en fin de
     * document s'il est absent. Une variable sans valeur reste visible entre crochets pour que la relecture la repère.
     */
    public static String assembler(String corps, List<Clause> clauses, Map<String, Object> valeurs, Set<String> clausesExclues) {
        StringBuilder blocClauses = new StringBuilder();
        for (Clause cl : clauses == null ? List.<Clause>of() : clauses) {
            if (clausesExclues.contains(cl.cle())) {
                continue;
            }
            blocClauses.append(cl.titre() == null ? "" : cl.titre()).append('\n')
                    .append(fusionner(cl.texte(), valeurs)).append("\n\n");
        }
        String texte = corps == null ? "" : corps;
        boolean marqueur = variables(texte).contains(MARQUEUR_CLAUSES);
        Map<String, Object> avecClauses = new HashMap<>(valeurs);
        avecClauses.put(MARQUEUR_CLAUSES, blocClauses.toString().trim());
        String fusion = fusionner(texte, avecClauses);
        return marqueur || blocClauses.length() == 0 ? fusion : fusion + "\n\n" + blocClauses.toString().trim();
    }

    static String fusionner(String gabarit, Map<String, Object> valeurs) {
        if (gabarit == null) {
            return "";
        }
        Matcher m = VARIABLE.matcher(gabarit);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            Object v = valeurs.get(m.group(1));
            String remplacement = v == null || String.valueOf(v).isBlank() ? "[" + m.group(1) + "]" : String.valueOf(v);
            m.appendReplacement(sb, Matcher.quoteReplacement(remplacement));
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
