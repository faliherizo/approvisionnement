package qc.levis.contrat.clm.application;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.clm.domain.Langue;
import qc.levis.contrat.clm.domain.NatureParticipation;

class FormulationInvitationTest {

    private static Map<String, Object> formuler(NatureParticipation nature, Langue langue) {
        Map<String, Object> variables = new HashMap<>();
        FormulationInvitation.ajouter(variables, nature, langue);
        return variables;
    }

    @Test
    void un_participant_qui_complete_n_est_pas_invite_a_signer() {
        assertEquals(Map.of("objet", "Informations à compléter", "action", "compléter vos informations dans"),
                formuler(NatureParticipation.COMPLETER, Langue.FR));
    }

    @Test
    void un_participant_qui_consulte_est_invite_a_consulter() {
        assertEquals(Map.of("objet", "Document à consulter", "action", "consulter"), formuler(NatureParticipation.CONSULTER, Langue.FR));
    }

    @Test
    void un_signataire_garde_la_formulation_d_origine_et_la_nature_absente_vaut_signer() {
        assertEquals(Map.of("objet", "Document à signer", "action", "signer"), formuler(NatureParticipation.SIGNER, Langue.FR));
        assertEquals(Map.of("objet", "Document à signer", "action", "signer"), formuler(null, Langue.FR));
    }

    @Test
    void la_formulation_suit_la_langue_du_destinataire() {
        assertEquals(Map.of("objet", "Information to complete", "action", "complete your information in"),
                formuler(NatureParticipation.COMPLETER, Langue.EN));
        assertEquals(Map.of("objet", "Document to review", "action", "review"), formuler(NatureParticipation.CONSULTER, Langue.EN));
        assertEquals(Map.of("objet", "Document to sign", "action", "sign"), formuler(NatureParticipation.SIGNER, Langue.EN));
    }
}
