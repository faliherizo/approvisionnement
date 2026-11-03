package qc.levis.contrat.clm.application;

import java.util.List;
import java.util.Map;
import qc.levis.contrat.clm.domain.Modele;
import qc.levis.contrat.regle.application.MoteurRegles;
import qc.levis.contrat.regle.domain.ConditionDef;
import qc.levis.contrat.regle.domain.RegleDef;

/** Frontière avec le moteur de règles partagé : les conditions du domaine CLM y sont traduites, jamais exposées. */
final class ReglesClm {

    private static final String CIBLE = "regle";

    private ReglesClm() {
    }

    static List<ConditionDef> versMoteur(List<Modele.Condition> conditions) {
        return conditions.stream().map(c -> new ConditionDef(c.champ(), c.operateur(), c.valeur())).toList();
    }

    /** Toutes les conditions sont vraies pour ces valeurs : la règle d'affaires d'une étape la rend requise. */
    static boolean satisfaites(List<Modele.Condition> conditions, Map<String, Object> valeurs) {
        RegleDef regle = new RegleDef("VISIBILITE", "ET", versMoteur(conditions), CIBLE, null);
        return !MoteurRegles.evaluer(List.of(regle), valeurs).champsMasques().contains(CIBLE);
    }
}
