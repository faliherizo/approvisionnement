package qc.levis.contrat.clm.application;

import java.util.EnumMap;
import java.util.Map;
import qc.levis.contrat.clm.domain.Langue;
import qc.levis.contrat.clm.domain.NatureParticipation;

/**
 * Formulation de l'invitation selon la nature de la participation (AF-48, RST-50) : un participant invité à compléter
 * ses informations, ou à consulter les documents, ne doit pas lire qu'il est invité à signer. Fournit au gabarit les
 * variables {{objet}} (sujet) et {{action}} (verbe), dans la langue du destinataire.
 */
final class FormulationInvitation {

    private record Formule(String objet, String action) {
    }

    private static final Map<NatureParticipation, Formule> FR = new EnumMap<>(Map.of(
            NatureParticipation.SIGNER, new Formule("Document à signer", "signer"),
            NatureParticipation.COMPLETER, new Formule("Informations à compléter", "compléter vos informations dans"),
            NatureParticipation.CONSULTER, new Formule("Document à consulter", "consulter")));
    private static final Map<NatureParticipation, Formule> EN = new EnumMap<>(Map.of(
            NatureParticipation.SIGNER, new Formule("Document to sign", "sign"),
            NatureParticipation.COMPLETER, new Formule("Information to complete", "complete your information in"),
            NatureParticipation.CONSULTER, new Formule("Document to review", "review")));

    private FormulationInvitation() {
    }

    static void ajouter(Map<String, Object> variables, NatureParticipation nature, Langue langue) {
        Formule f = (langue == Langue.EN ? EN : FR).get(nature == null ? NatureParticipation.SIGNER : nature);
        variables.put("objet", f.objet());
        variables.put("action", f.action());
    }
}
