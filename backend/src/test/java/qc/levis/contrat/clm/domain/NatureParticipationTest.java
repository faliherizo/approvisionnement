package qc.levis.contrat.clm.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import org.junit.jupiter.api.Test;

/** AF-48 (RST-50) : nature de participation, actions refusées hors nature et participation accomplie sans signature. */
class NatureParticipationTest {

    private static final Instant T0 = Instant.parse("2026-11-26T10:00:00Z");
    private static final Invitation.Poste POSTE = new Invitation.Poste("10.0.0.1", "Navigateur");

    private static Invitation verifiee() {
        Invitation i = new Invitation(1L, 2L, 3L, 4L, "h", T0.plusSeconds(3600), Identite.Niveau.QUESTION);
        i.verifier(T0);
        return i;
    }

    private static Participant participant() {
        return new Participant(1L, 2L, "Lise", "lise@x.example", null, "TEMOIN", true, Identite.Niveau.QUESTION);
    }

    @Test
    void la_nature_saisie_est_lue_sans_casse_et_signer_par_defaut() {
        assertEquals(NatureParticipation.SIGNER, NatureParticipation.de(null));
        assertEquals(NatureParticipation.SIGNER, NatureParticipation.de("  "));
        assertEquals(NatureParticipation.CONSULTER, NatureParticipation.de(" consulter "));
        assertEquals(NatureParticipation.COMPLETER, NatureParticipation.de("COMPLETER"));
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> NatureParticipation.de("APPROUVER"));
        assertTrue(e.getMessage().contains("CONSULTER, COMPLETER ou SIGNER"));
    }

    @Test
    void une_action_hors_nature_est_refusee_par_une_exception_typee() {
        assertDoesNotThrow(() -> NatureParticipation.SIGNER.exiger(NatureParticipation.SIGNER));
        NatureParticipation.ActionHorsNature refus = assertThrows(NatureParticipation.ActionHorsNature.class,
                () -> NatureParticipation.CONSULTER.exiger(NatureParticipation.SIGNER));
        assertEquals(NatureParticipation.CONSULTER, refus.nature());
        assertEquals(NatureParticipation.SIGNER, refus.action());
        assertEquals("Action refusée : votre participation vous permet de consulter les documents, pas de signer.", refus.getMessage());
        assertInstanceOf(IllegalStateException.class, refus, "refus 409 au portail, comme un lien déjà utilisé");
        assertThrows(NatureParticipation.ActionHorsNature.class, () -> NatureParticipation.COMPLETER.exiger(NatureParticipation.CONSULTER));
    }

    @Test
    void seul_le_participant_consulter_n_a_aucun_champ_a_remplir() {
        assertFalse(NatureParticipation.CONSULTER.remplitDesChamps());
        assertTrue(NatureParticipation.COMPLETER.remplitDesChamps());
        assertTrue(NatureParticipation.SIGNER.remplitDesChamps());
    }

    @Test
    void le_participant_porte_sa_nature_signer_par_defaut() {
        Participant p = participant();
        assertEquals(NatureParticipation.SIGNER, p.getNature(), "rétrocompatible : les participants antérieurs signent");
        p.choisirNature(NatureParticipation.CONSULTER);
        assertEquals(NatureParticipation.CONSULTER, p.getNature());
        assertDoesNotThrow(() -> p.exigerAction(NatureParticipation.CONSULTER));
        assertThrows(NatureParticipation.ActionHorsNature.class, () -> p.exigerAction(NatureParticipation.COMPLETER));
        p.choisirNature(null);
        assertEquals(NatureParticipation.SIGNER, p.getNature());
    }

    @Test
    void la_consultation_attestee_clot_l_invitation_sans_signature() {
        Invitation envoyee = new Invitation(1L, 2L, 3L, 4L, "h", T0.plusSeconds(3600), Identite.Niveau.QUESTION);
        assertThrows(IllegalStateException.class, () -> envoyee.attesterConsultation(POSTE, "{}", T0), "identité d'abord");
        Invitation i = verifiee();
        assertFalse(i.estAccomplie());
        assertNull(i.actionAccomplie());
        i.attesterConsultation(POSTE, "{\"action\":\"CONSULTATION\"}", T0);
        assertEquals(Invitation.Statut.CONSULTEE, i.getStatut());
        assertTrue(i.estAccomplie());
        assertEquals("CONSULTATION", i.actionAccomplie());
        assertEquals(T0, i.getConsentementLe(), "l'attestation tient lieu de consentement");
        assertEquals(T0, i.getSigneeLe());
        assertEquals("10.0.0.1", i.getIp());
        assertNull(i.getNomSigne(), "aucune signature");
        assertEquals("{\"action\":\"CONSULTATION\"}", i.getAttestationJson());
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> i.exigerOuverte(T0));
        assertEquals("Lien déjà utilisé.", e.getMessage());
    }

    @Test
    void la_completion_conserve_les_valeurs_soumises_sans_signature() {
        Invitation i = verifiee();
        i.completer("{\"neq\":\"1234\"}", POSTE, "{\"action\":\"COMPLETION\"}", T0);
        assertEquals(Invitation.Statut.COMPLETEE, i.getStatut());
        assertEquals("COMPLETION", i.actionAccomplie());
        assertEquals("{\"neq\":\"1234\"}", i.getChampsJson());
        assertNull(i.getConsentementLe(), "aucun consentement à signer");
        assertThrows(IllegalStateException.class, () -> i.exigerOuverte(T0));
        Invitation envoyee = new Invitation(1L, 2L, 3L, 4L, "h", T0.plusSeconds(3600), Identite.Niveau.QUESTION);
        IllegalStateException refus = assertThrows(IllegalStateException.class, () -> envoyee.completer("{}", POSTE, "{}", T0));
        assertEquals("Identité à vérifier avant de soumettre vos champs.", refus.getMessage());
    }

    @Test
    void une_participation_accomplie_n_est_jamais_annulee_mais_un_nouveau_cycle_l_invalide() {
        Invitation consultee = verifiee();
        consultee.attesterConsultation(POSTE, "{}", T0);
        consultee.annuler();
        assertEquals(Invitation.Statut.CONSULTEE, consultee.getStatut());
        consultee.invalider();
        assertEquals(Invitation.Statut.INVALIDEE, consultee.getStatut());
        assertNull(consultee.actionAccomplie());
        Invitation completee = verifiee();
        completee.completer("{}", POSTE, "{}", T0);
        completee.annuler();
        assertEquals(Invitation.Statut.COMPLETEE, completee.getStatut());
        Invitation signee = verifiee();
        signee.consentir(T0);
        signee.signer("Lise", "ip", "ua", "{}", "{}", T0);
        assertEquals("SIGNATURE", signee.actionAccomplie());
        Invitation refusee = verifiee();
        refusee.refuser("motif", "ip", T0);
        refusee.annuler();
        assertEquals(Invitation.Statut.REFUSEE, refusee.getStatut());
        assertFalse(refusee.estAccomplie());
    }
}
