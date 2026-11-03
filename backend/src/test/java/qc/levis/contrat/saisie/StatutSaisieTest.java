package qc.levis.contrat.saisie;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import qc.levis.contrat.saisie.domain.StatutSaisie;

/** Tests purs de la machine à états d'une demande (architecture §15.1). */
class StatutSaisieTest {

    @Test
    void transitions_autorisees_du_cycle_nominal() {
        assertTrue(StatutSaisie.BROUILLON.peutAllerVers(StatutSaisie.SOUMIS));
        assertTrue(StatutSaisie.SOUMIS.peutAllerVers(StatutSaisie.EN_TRAITEMENT));
        assertTrue(StatutSaisie.EN_TRAITEMENT.peutAllerVers(StatutSaisie.VALIDE));
        assertTrue(StatutSaisie.EN_TRAITEMENT.peutAllerVers(StatutSaisie.A_COMPLETER));
        assertTrue(StatutSaisie.A_COMPLETER.peutAllerVers(StatutSaisie.SOUMIS));
        assertTrue(StatutSaisie.VALIDE.peutAllerVers(StatutSaisie.CLOTURE));
        assertTrue(StatutSaisie.REJETE.peutAllerVers(StatutSaisie.CLOTURE));
        assertTrue(StatutSaisie.BROUILLON.peutAllerVers(StatutSaisie.ANNULE));
    }

    @Test
    void transitions_interdites() {
        assertFalse(StatutSaisie.BROUILLON.peutAllerVers(StatutSaisie.VALIDE), "pas de validation d'un brouillon");
        assertFalse(StatutSaisie.CLOTURE.peutAllerVers(StatutSaisie.SOUMIS), "état final");
        assertFalse(StatutSaisie.ANNULE.peutAllerVers(StatutSaisie.SOUMIS), "état final");
        assertFalse(StatutSaisie.VALIDE.peutAllerVers(StatutSaisie.REJETE));
    }

    @Test
    void etats_finaux() {
        assertTrue(StatutSaisie.CLOTURE.estTermine());
        assertTrue(StatutSaisie.ANNULE.estTermine());
        assertFalse(StatutSaisie.EN_TRAITEMENT.estTermine());
        assertFalse(StatutSaisie.VALIDE.estTermine());
    }
}
