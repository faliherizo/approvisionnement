package qc.levis.contrat.vieprivee.domain;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/** Un renseignement personnel détenu : sa référence dans la plateforme, sa date et les valeurs qui concernent la personne. */
public record Renseignement(String reference, Instant date, Map<String, String> valeurs) {

    public Renseignement {
        valeurs = Map.copyOf(valeurs);
    }

    /** Valeurs données par paires libellé, valeur ; une valeur absente est communiquée vide. */
    public static Renseignement de(String reference, Instant date, String... libellesValeurs) {
        if (libellesValeurs.length % 2 != 0) throw new IllegalArgumentException("Libellés et valeurs vont par paires.");
        Map<String, String> v = new LinkedHashMap<>();
        for (int i = 0; i < libellesValeurs.length; i += 2) v.put(libellesValeurs[i], libellesValeurs[i + 1] == null ? "" : libellesValeurs[i + 1]);
        return new Renseignement(reference, date, v);
    }
}
