package qc.levis.contrat.processus;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.processus.application.MoteurWorkflow;
import qc.levis.contrat.processus.domain.ConditionEtape;
import qc.levis.contrat.processus.domain.EtapeDef;
import qc.levis.contrat.processus.domain.WorkflowDef;

/** Tests purs du routage de workflow (écran 5 « Montant > seuil ? »). */
class MoteurWorkflowTest {

    private EtapeDef etape(String code, List<ConditionEtape> conditions) {
        return new EtapeDef(code, code, "APPROBATION", "APPRO", null, 3, true, conditions);
    }

    private final WorkflowDef workflow = new WorkflowDef(List.of(
            etape("gestionnaire", List.of()),
            etape("finance", List.of(new ConditionEtape("montant", "SUP", "10000")))));

    @Test
    void au_dessus_du_seuil_les_deux_etapes_sappliquent() {
        List<EtapeDef> appl = MoteurWorkflow.etapesApplicables(workflow, Map.of("montant", "25000"));
        assertEquals(List.of("gestionnaire", "finance"), appl.stream().map(EtapeDef::code).toList());
    }

    @Test
    void sous_le_seuil_la_finance_est_sautee() {
        List<EtapeDef> appl = MoteurWorkflow.etapesApplicables(workflow, Map.of("montant", "500"));
        assertEquals(List.of("gestionnaire"), appl.stream().map(EtapeDef::code).toList());
    }

    /** Vrai si une étape à condition unique s'applique (via l'API publique). */
    private boolean applique(String champ, String op, String valeur, Map<String, Object> valeurs) {
        WorkflowDef w = new WorkflowDef(List.of(etape("e", List.of(new ConditionEtape(champ, op, valeur)))));
        return !MoteurWorkflow.etapesApplicables(w, valeurs).isEmpty();
    }

    @Test
    void operateurs_de_comparaison_et_egalite() {
        assertTrue(applique("m", "SUPEGAL", "10", Map.of("m", "10")));
        assertTrue(applique("m", "INF", "10", Map.of("m", "9")));
        assertFalse(applique("m", "SUP", "10", Map.of("m", "10")));
        assertTrue(applique("t", "EGAL", "formation", Map.of("t", "formation")));
        assertTrue(applique("t", "CONTIENT", "orm", Map.of("t", "formation")));
        assertTrue(applique("x", "VIDE", "", Map.of()));
        assertTrue(applique("x", "NON_VIDE", "", Map.of("x", "a")));
    }

    @Test
    void valeur_non_numerique_ne_passe_pas_une_comparaison() {
        assertFalse(applique("m", "SUP", "10", Map.of("m", "abc")));
    }
}
