package qc.levis.contrat.redaction.domain;

import java.util.Locale;
import qc.levis.contrat.commun.domain.Enums;

/**
 * Critères de la recherche multicritère des clauses (Lévis EF1, LEV-C-02) : terme, portée (catégorie), thématique
 * (section du cahier), type de contrat auquel la clause s'applique, clauses normatives seulement. Un critère absent ne
 * filtre rien.
 *
 * @param terme             recherché dans le code, le titre et le corps, sans tenir compte de la casse
 * @param categorie         portée : standard, optionnelle ou personnalisée
 * @param typeContrat       une clause s'applique à ce type si elle ne pose aucune condition de type, ou si l'une désigne ce type
 * @param thematique        section du cahier (ex. « Assurances »), sans tenir compte de la casse
 * @param normativesSeulement ne retient que les clauses exigées par la loi
 */
public record CriteresRechercheClause(String terme, Enums.ClauseCategorie categorie, Enums.TypeContrat typeContrat,
                                      String thematique, boolean normativesSeulement) {

    public CriteresRechercheClause {
        terme = normaliser(terme);
        thematique = normaliser(thematique);
    }

    public boolean retient(Clause c) {
        return (terme.isEmpty() || contient(c.getCode(), terme) || contient(c.getTitre(), terme) || contient(c.getCorps(), terme))
                && (categorie == null || c.getCategorie() == categorie)
                && (thematique.isEmpty() || thematique.equals(normaliser(c.getSection())))
                && (!normativesSeulement || c.isNormative())
                && (typeContrat == null || sApplique(c, typeContrat));
    }

    /** Sans condition de type, la clause vaut pour tous les types ; sinon, l'une de ses conditions doit désigner ce type. */
    static boolean sApplique(Clause c, Enums.TypeContrat type) {
        var conditionsDeType = c.getConditions().stream().filter(k -> k.getAttribut() == Enums.AttributCondition.TYPE_CONTRAT).toList();
        return conditionsDeType.isEmpty() || conditionsDeType.stream().anyMatch(k -> type.name().equalsIgnoreCase(k.getValeur()));
    }

    private static boolean contient(String champ, String terme) {
        return champ != null && champ.toLowerCase(Locale.FRENCH).contains(terme);
    }

    private static String normaliser(String texte) {
        return texte == null ? "" : texte.strip().toLowerCase(Locale.FRENCH);
    }
}
