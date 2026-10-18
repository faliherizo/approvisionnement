package qc.levis.contrat.redaction.application;

import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import qc.levis.contrat.commun.domain.Enums;
import qc.levis.contrat.redaction.domain.Clause;
import qc.levis.contrat.redaction.domain.ClauseCondition;
import qc.levis.contrat.redaction.domain.Document;

/**
 * Moteur d'adaptation automatique des clauses. Évalue les conditions d'inclusion
 * (données maintenues par les juristes) contre le contexte du document :
 * type de contrat, mode de sollicitation, valeur estimée, contexte projet.
 */
@Service
public class RuleEngine {

    /** Une clause est éligible si TOUTES ses conditions sont satisfaites. */
    public boolean estEligible(Clause clause, Document doc) {
        for (ClauseCondition c : clause.getConditions()) {
            if (!conditionSatisfaite(c, doc)) {
                return false;
            }
        }
        return true;
    }

    private boolean conditionSatisfaite(ClauseCondition c, Document doc) {
        String attendu = c.getValeur();
        switch (c.getAttribut()) {
            case TYPE_CONTRAT:
                return doc.getType() != null && doc.getType().name().equals(attendu);
            case MODE_SOLLICITATION:
                return doc.getModeSollicitation() != null
                        && doc.getModeSollicitation().name().equals(attendu);
            case CONTEXTE:
                return doc.getContexte() != null
                        && doc.getContexte().toLowerCase().contains(attendu.toLowerCase());
            case VALEUR_MIN:
                return doc.getValeurEstimee() != null
                        && doc.getValeurEstimee().compareTo(new BigDecimal(attendu)) >= 0;
            case VALEUR_MAX:
                return doc.getValeurEstimee() != null
                        && doc.getValeurEstimee().compareTo(new BigDecimal(attendu)) <= 0;
            default:
                return false;
        }
    }

    /** Détermine le mode de sollicitation suggéré selon la valeur (seuils indicatifs). */
    public Enums.ModeSollicitation modeSuggere(BigDecimal valeur) {
        if (valeur == null) return null;
        // Seuils indicatifs - à paramétrer selon LCOP/LCM/LCV en vigueur.
        if (valeur.compareTo(new BigDecimal("121200")) >= 0) {
            return Enums.ModeSollicitation.APPEL_OFFRES_PUBLIC;
        }
        if (valeur.compareTo(new BigDecimal("25000")) >= 0) {
            return Enums.ModeSollicitation.DEMANDE_PRIX;
        }
        return Enums.ModeSollicitation.GRE_A_GRE;
    }
}
