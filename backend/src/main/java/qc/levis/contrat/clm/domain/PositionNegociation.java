package qc.levis.contrat.clm.domain;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Position recommandée sur une proposition de la contrepartie (RST-55) : une suggestion, jamais une décision. Seule une
 * personne de l'organisation accepte, refuse ou contre-propose.
 */
public enum PositionNegociation {
    ACCEPTER, CONTRE_PROPOSER, REFUSER;

    private static final Pattern MOT = Pattern.compile("\\b(ACCEPTER|REFUSER|CONTRE[ _-]PROPOSER)\\b");

    /** Première position nommée dans une réponse libre (celle d'un modèle de langage) ; aucune si elle n'en nomme pas. */
    public static Optional<PositionNegociation> lire(String reponse) {
        if (reponse == null) {
            return Optional.empty();
        }
        Matcher m = MOT.matcher(reponse.toUpperCase(Locale.ROOT));
        if (!m.find()) {
            return Optional.empty();
        }
        return Optional.of(valueOf(m.group(1).replace(' ', '_').replace('-', '_')));
    }
}
