package qc.levis.contrat.service;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.commun.domain.Enums;
import qc.levis.contrat.redaction.application.RuleEngine;
import qc.levis.contrat.redaction.domain.Clause;
import qc.levis.contrat.redaction.domain.ClauseCondition;
import qc.levis.contrat.redaction.domain.Document;
import static org.junit.jupiter.api.Assertions.*;

class RuleEngineTest {

    private final RuleEngine moteur = new RuleEngine();

    private Clause clauseAvec(Enums.AttributCondition attr, String valeur) {
        Clause c = new Clause();
        c.setCode("X");
        ClauseCondition cc = new ClauseCondition();
        cc.setAttribut(attr);
        cc.setValeur(valeur);
        c.addCondition(cc);
        return c;
    }

    private Document doc(Enums.TypeContrat type, Enums.ModeSollicitation mode, String valeur, String contexte) {
        Document d = new Document();
        d.setType(type);
        d.setModeSollicitation(mode);
        if (valeur != null) d.setValeurEstimee(new BigDecimal(valeur));
        d.setContexte(contexte);
        return d;
    }

    @Test
    void clauseSansCondition_estToujoursEligible() {
        assertTrue(moteur.estEligible(new Clause(), doc(Enums.TypeContrat.GRE_A_GRE, null, null, null)));
    }

    @Test
    void condition_modeSollicitation() {
        Clause c = clauseAvec(Enums.AttributCondition.MODE_SOLLICITATION, "APPEL_OFFRES_PUBLIC");
        assertTrue(moteur.estEligible(c, doc(null, Enums.ModeSollicitation.APPEL_OFFRES_PUBLIC, null, null)));
        assertFalse(moteur.estEligible(c, doc(null, Enums.ModeSollicitation.GRE_A_GRE, null, null)));
    }

    @Test
    void condition_typeContrat() {
        Clause c = clauseAvec(Enums.AttributCondition.TYPE_CONTRAT, "BAIL");
        assertTrue(moteur.estEligible(c, doc(Enums.TypeContrat.BAIL, null, null, null)));
        assertFalse(moteur.estEligible(c, doc(Enums.TypeContrat.GRE_A_GRE, null, null, null)));
    }

    @Test
    void condition_valeurMinEtMax() {
        Clause min = clauseAvec(Enums.AttributCondition.VALEUR_MIN, "1000000");
        assertTrue(moteur.estEligible(min, doc(null, null, "2000000", null)));
        assertFalse(moteur.estEligible(min, doc(null, null, "500000", null)));
        assertFalse(moteur.estEligible(min, doc(null, null, null, null)));

        Clause max = clauseAvec(Enums.AttributCondition.VALEUR_MAX, "1000000");
        assertTrue(moteur.estEligible(max, doc(null, null, "500000", null)));
        assertFalse(moteur.estEligible(max, doc(null, null, "2000000", null)));
    }

    @Test
    void condition_contexteContient() {
        Clause c = clauseAvec(Enums.AttributCondition.CONTEXTE, "voirie");
        assertTrue(moteur.estEligible(c, doc(null, null, null, "Travaux de VOIRIE majeurs")));
        assertFalse(moteur.estEligible(c, doc(null, null, null, "Achat mobilier")));
    }

    @Test
    void modeSuggere_selonSeuils() {
        assertEquals(Enums.ModeSollicitation.APPEL_OFFRES_PUBLIC, moteur.modeSuggere(new BigDecimal("200000")));
        assertEquals(Enums.ModeSollicitation.DEMANDE_PRIX, moteur.modeSuggere(new BigDecimal("50000")));
        assertEquals(Enums.ModeSollicitation.GRE_A_GRE, moteur.modeSuggere(new BigDecimal("1000")));
        assertNull(moteur.modeSuggere(null));
    }
}
