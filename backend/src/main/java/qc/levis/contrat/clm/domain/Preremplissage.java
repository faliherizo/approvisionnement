package qc.levis.contrat.clm.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Préremplissage d'un contrat depuis le dossier source (ST-06, UI-10) : chaque valeur garde sa provenance et sa
 * date de récupération ; une saisie manuelle prévaut mais un écart avec la source est signalé comme conflit.
 */
public final class Preremplissage {

    public enum Provenance { SALESFORCE, MANUEL, DEFAUT }

    public record Valeur(Object valeur, Provenance provenance, Instant recupereLe) {
    }

    public record Conflit(String cle, Object valeurSource, Object valeurManuelle) {
    }

    public record Resultat(Map<String, Valeur> valeurs, List<String> manquants, List<Conflit> conflits) {
        public Map<String, Object> brutes() {
            Map<String, Object> m = new LinkedHashMap<>();
            valeurs.forEach((k, v) -> m.put(k, v.valeur()));
            return m;
        }
    }

    private Preremplissage() {
    }

    public static Resultat appliquer(List<Modele.Champ> champs, Map<String, Object> source, Instant recupereLe,
                                     Map<String, Object> manuelles) {
        Map<String, Object> src = source == null ? Map.of() : source;
        Map<String, Object> man = manuelles == null ? Map.of() : manuelles;
        Map<String, Valeur> valeurs = new LinkedHashMap<>();
        List<String> manquants = new ArrayList<>();
        List<Conflit> conflits = new ArrayList<>();
        for (Modele.Champ c : champs) {
            if (!c.completeALaGeneration()) {
                continue;
            }
            Object deSource = c.source() == null ? null : src.get(c.source());
            Object manuelle = man.get(c.cle());
            if (present(manuelle)) {
                valeurs.put(c.cle(), new Valeur(manuelle, Provenance.MANUEL, null));
                if (present(deSource) && !Objects.equals(String.valueOf(deSource), String.valueOf(manuelle))) {
                    conflits.add(new Conflit(c.cle(), deSource, manuelle));
                }
            } else if (present(deSource)) {
                valeurs.put(c.cle(), new Valeur(deSource, Provenance.SALESFORCE, recupereLe));
            } else if (present(c.valeurDefaut())) {
                valeurs.put(c.cle(), new Valeur(c.valeurDefaut(), Provenance.DEFAUT, null));
            } else if (c.obligatoire()) {
                manquants.add(c.cle());
            }
        }
        return new Resultat(valeurs, manquants, conflits);
    }

    private static boolean present(Object v) {
        return v != null && !String.valueOf(v).isBlank();
    }
}
