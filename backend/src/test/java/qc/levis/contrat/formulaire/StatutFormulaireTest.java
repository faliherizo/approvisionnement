package qc.levis.contrat.formulaire;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import qc.levis.contrat.formulaire.domain.StatutFormulaire;

/** Tests purs du cycle de vie d'une version de formulaire. */
class StatutFormulaireTest {

    @Test
    void transitions_autorisees() {
        assertTrue(StatutFormulaire.BROUILLON.peutAllerVers(StatutFormulaire.EN_APPROBATION));
        assertTrue(StatutFormulaire.EN_APPROBATION.peutAllerVers(StatutFormulaire.PUBLIE));
        assertTrue(StatutFormulaire.EN_APPROBATION.peutAllerVers(StatutFormulaire.BROUILLON));
        assertTrue(StatutFormulaire.PUBLIE.peutAllerVers(StatutFormulaire.ARCHIVE));
        assertTrue(StatutFormulaire.PUBLIE.peutAllerVers(StatutFormulaire.BROUILLON));
    }

    @Test
    void transitions_interdites() {
        assertFalse(StatutFormulaire.BROUILLON.peutAllerVers(StatutFormulaire.PUBLIE));
        assertFalse(StatutFormulaire.ARCHIVE.peutAllerVers(StatutFormulaire.BROUILLON));
        assertFalse(StatutFormulaire.PUBLIE.peutAllerVers(StatutFormulaire.EN_APPROBATION));
    }
}
