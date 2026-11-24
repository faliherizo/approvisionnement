package qc.levis.contrat.interop.domain;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Table de traduction entre les noms de champs d'un système hérité et les clés d'un formulaire (CHU-8.04, ADR-021).
 * Elle traduit dans les deux sens ; un nom inconnu est refusé, jamais ignoré : une perte silencieuse serait pire.
 */
public final class CorrespondanceEchange {

    public static final int NOM_MAX = 120;

    private final Map<String, String> versCle;

    private CorrespondanceEchange(Map<String, String> versCle) {
        this.versCle = Collections.unmodifiableMap(new LinkedHashMap<>(versCle));
    }

    /** Correspondance valide pour un formulaire dont les clés de champ sont données. */
    public static CorrespondanceEchange de(Map<String, String> ancienVersCle, Set<String> clesFormulaire) {
        if (ancienVersCle == null || ancienVersCle.isEmpty()) {
            throw new CorrespondanceInvalide("Au moins une correspondance « ancien nom → champ » est requise.");
        }
        Map<String, String> propre = new LinkedHashMap<>();
        Set<String> cibles = new HashSet<>();
        ancienVersCle.forEach((ancien, cle) -> {
            String a = nom(ancien, "Ancien nom");
            String c = nom(cle, "Champ cible de « " + a + " »");
            if (!clesFormulaire.contains(c)) {
                throw new CorrespondanceInvalide("Le champ « " + c + " » n'existe pas dans le formulaire.");
            }
            if (!cibles.add(c)) {
                throw new CorrespondanceInvalide("Le champ « " + c + " » est la cible de plusieurs anciens noms.");
            }
            propre.put(a, c);
        });
        return new CorrespondanceEchange(propre);
    }

    /** Correspondance relue de la base, déjà validée à son enregistrement. */
    static CorrespondanceEchange relue(Map<String, String> ancienVersCle) {
        return new CorrespondanceEchange(ancienVersCle);
    }

    /** Données d'un enregistrement hérité, sous les clés du formulaire ; un nom inconnu est refusé. */
    public Map<String, Object> versFormulaire(Map<String, Object> ancien) {
        Map<String, Object> donnees = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : ancien.entrySet()) {
            String cle = versCle.get(e.getKey());
            if (cle == null) {
                throw new ChampHeriteInconnu(e.getKey());
            }
            donnees.put(cle, e.getValue());
        }
        return donnees;
    }

    /** Données d'une fiche sous les anciens noms ; un champ que l'ancien système ne connaissait pas est omis. */
    public Map<String, Object> versAncien(Map<String, Object> donnees) {
        Map<String, Object> ancien = new LinkedHashMap<>();
        versCle.forEach((a, cle) -> {
            if (donnees.containsKey(cle)) {
                ancien.put(a, donnees.get(cle));
            }
        });
        return ancien;
    }

    public Map<String, String> table() {
        return versCle;
    }

    private static String nom(String brut, String quoi) {
        String n = brut == null ? "" : brut.strip();
        if (n.isEmpty() || n.length() > NOM_MAX) {
            throw new CorrespondanceInvalide(quoi + " : 1 à " + NOM_MAX + " caractères.");
        }
        return n;
    }

    /** Correspondance refusée à l'enregistrement. */
    public static class CorrespondanceInvalide extends IllegalArgumentException {
        public CorrespondanceInvalide(String message) {
            super(message);
        }
    }

    /** Un enregistrement porte un nom absent de la correspondance. */
    public static class ChampHeriteInconnu extends IllegalArgumentException {
        public ChampHeriteInconnu(String nom) {
            super("Champ « " + nom + " » inconnu de la correspondance d'échange.");
        }
    }
}
