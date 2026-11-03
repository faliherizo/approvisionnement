package qc.levis.contrat.clm.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Invariants portés par les entités CLM : invitation, boîte d'envoi, étape, document, analyse, modèle. */
class EntitesClmTest {

    private static final Instant T0 = Instant.parse("2026-11-02T10:00:00Z");

    private static Invitation invitation() {
        return new Invitation(1L, 2L, 3L, 4L, "h", T0.plusSeconds(3600), Identite.Niveau.CODE_SMS);
    }

    @Test
    void invitation_impose_verification_puis_consentement() {
        Invitation i = invitation();
        assertThrows(IllegalStateException.class, () -> i.consentir(T0));
        assertThrows(IllegalStateException.class, () -> i.signer("n", null, null, "{}", "{}", T0));
        i.preparerCode("c", T0.plusSeconds(600));
        assertEquals("c", i.getCodeHash());
        i.verifier(T0);
        assertNull(i.getCodeHash());
        assertThrows(IllegalStateException.class, () -> i.signer("n", null, null, "{}", "{}", T0), "consentement manquant");
        i.consentir(T0);
        i.signer("Nom", "1.2.3.4", "UA", "{}", "{}", T0);
        assertEquals(Invitation.Statut.SIGNEE, i.getStatut());
        assertThrows(IllegalStateException.class, () -> i.exigerOuverte(T0));
        i.annuler();
        assertEquals(Invitation.Statut.SIGNEE, i.getStatut(), "une signature n'est jamais annulée");
    }

    @Test
    void invitation_bloquee_expiree_annulee_refusee() {
        Invitation i = invitation();
        for (int n = 0; n < Identite.TENTATIVES_MAX; n++) {
            i.echecVerification();
        }
        assertEquals(Invitation.Statut.BLOQUEE, i.getStatut());
        assertThrows(IllegalStateException.class, () -> i.exigerOuverte(T0));
        Invitation e = invitation();
        assertTrue(e.estExpiree(T0.plusSeconds(7200)));
        assertThrows(IllegalStateException.class, () -> e.exigerOuverte(T0.plusSeconds(7200)));
        assertDoesNotThrow(() -> e.exigerOuverte(T0));
        e.annuler();
        assertThrows(IllegalStateException.class, () -> e.exigerOuverte(T0));
        Invitation r = invitation();
        r.refuser("motif", "ip", T0);
        assertEquals("motif", r.getMotif());
        assertThrows(IllegalStateException.class, () -> r.exigerOuverte(T0));
    }

    @Test
    void message_sortant_reprises_puis_echec_et_relance() {
        MessageSortant m = new MessageSortant(1L, 2L, "sf", "LANCEE", "k", "c", "{}", T0);
        m.echouer("x".repeat(2000), T0);
        assertEquals(1, m.getTentatives());
        assertEquals(1000, m.getDerniereErreur().length());
        assertEquals(T0.plusSeconds(60), m.getProchainEssai());
        for (int i = 1; i < Relances.REPRISES_MAX; i++) {
            m.echouer(null, T0);
        }
        assertEquals(MessageSortant.Statut.ECHEC, m.getStatut());
        m.relancer(T0);
        assertEquals(MessageSortant.Statut.EN_ATTENTE, m.getStatut());
        assertEquals(0, m.getTentatives());
        m.reussir(T0);
        assertEquals(T0, m.getEnvoyeLe());
        assertThrows(IllegalStateException.class, () -> m.relancer(T0), "événement dupliqué");
    }

    @Test
    void etape_activation_rappels_et_reinitialisation() {
        EtapeTrousse e = new EtapeTrousse(1L, 2L, new Parcours.DefinitionEtape("k", " ", 1, Parcours.TypeEtape.VALIDATION, List.of(), 1, null, 3), "[]");
        assertEquals("k", e.getLibelle());
        e.activer(T0);
        assertEquals(T0.plusSeconds(3 * 86_400), e.getEcheance());
        e.noterRappel(T0);
        e.signalerRetard();
        assertEquals(1, e.getRappelsEnvoyes());
        assertTrue(e.isRetardSignale());
        e.exigerModifiable();
        e.terminer(Parcours.StatutEtape.APPROUVEE, T0);
        assertThrows(IllegalStateException.class, e::exigerModifiable);
        e.reinitialiser();
        assertEquals(Parcours.StatutEtape.EN_ATTENTE, e.getStatut());
        assertNull(e.getEcheance());
    }

    @Test
    void document_signe_verrouille_et_analyse_revisee_une_fois() {
        DocumentTrousse d = new DocumentTrousse(1L, 2L, "c", 1, "a.pdf", FormatDocument.Type.PDF, "%PDF-".getBytes(), "u", null, T0);
        assertEquals(5, d.getTaille());
        assertTrue(d.estCourant());
        d.setStatut(DocumentTrousse.Statut.SIGNE);
        assertThrows(IllegalStateException.class, d::exigerNonVerrouille);
        d.setStatut(DocumentTrousse.Statut.REMPLACE);
        assertFalse(d.estCourant());

        AnalyseIa a = new AnalyseIa(1L, 2L, 3L, "{}", "u", T0);
        assertThrows(IllegalArgumentException.class, () -> a.reviser(true, "r", " ", T0));
        a.reviser(false, "r", " non conforme ", T0);
        assertEquals(AnalyseIa.Statut.REJETEE, a.getStatut());
        assertEquals("non conforme", a.getJustification());
        assertThrows(IllegalStateException.class, () -> a.reviser(true, "r", "x", T0));
    }

    @Test
    void trousse_et_modele_proteges_hors_brouillon() {
        Trousse t = new Trousse(1L, "R", "T", "p", T0);
        t.exigerModifiable();
        t.changerStatut(StatutTrousse.EN_TRAITEMENT, T0.plusSeconds(5));
        assertEquals(T0.plusSeconds(5), t.getMajLe());
        assertThrows(IllegalStateException.class, t::exigerModifiable);

        ModeleContrat m = new ModeleContrat(1L, "cle", 1, "M", "p", T0);
        m.exigerBrouillon();
        m.changerStatut(Modele.Statut.EN_REVISION, T0);
        assertThrows(IllegalStateException.class, m::exigerBrouillon);
    }
}
