package qc.levis.contrat.clm.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/** Règles du domaine ajoutées pour la conformité : groupes, conservation, destruction, invalidation, trousse ouverte. */
class ConformiteDomaineTest {

    private static final Instant T0 = Instant.parse("2026-11-11T15:00:00Z");
    private static final ZoneId QC = ZoneId.of("America/Toronto");

    @Test
    void un_groupe_a_un_code_normalise_et_un_libelle_borne() {
        GroupeClm g = GroupeClm.creer(7L, " juridique_iq ", " Juristes ", "admin", T0);
        assertEquals("JURIDIQUE_IQ", g.getCode());
        assertEquals("Juristes", g.getLibelle());
        assertEquals("admin", g.getCreePar());
        assertEquals(T0, g.getCreeLe());
        for (String code : new String[]{null, "", "1ABC", "A-B", "A".repeat(61)}) {
            assertThrows(IllegalArgumentException.class, () -> GroupeClm.creer(7L, code, "L", "a", T0), String.valueOf(code));
        }
        assertThrows(IllegalArgumentException.class, () -> GroupeClm.creer(7L, "A", null, "a", T0));
        assertThrows(IllegalArgumentException.class, () -> GroupeClm.creer(7L, "A", "x".repeat(121), "a", T0));
        ReflectionTestUtils.setField(g, "id", 3L);
        MembreGroupeClm m = new MembreGroupeClm(g, " Validateur@IQ.example ");
        assertEquals("validateur@iq.example", m.getCourriel());
        assertEquals("JURIDIQUE_IQ", m.getGroupeCode());
        assertEquals(3L, m.getGroupeId());
        assertNull(m.getId());
    }

    @Test
    void une_regle_fixe_l_echeance_dans_le_fuseau_de_l_organisation_sauf_conservation_permanente() {
        RegleConservation r = new RegleConservation(7L, " GARANTIE ");
        assertEquals("GARANTIE", r.getTypeContrat());
        r.definir(10, RegleConservation.SortFinal.VERSER, "admin", T0);
        assertEquals(LocalDate.of(2036, 11, 11), r.echeance(T0, QC));
        assertEquals(LocalDate.of(2036, 11, 10), r.echeance(Instant.parse("2026-11-11T03:00:00Z"), QC), "date locale de clôture");
        assertNull(r.echeance(null, QC));
        r.definir(5, RegleConservation.SortFinal.CONSERVER, "admin", T0);
        assertNull(r.echeance(T0, QC), "conservation permanente");
        assertNull(new RegleConservation(7L, " ").getTypeContrat(), "règle par défaut");
        assertNull(new RegleConservation(7L, null).getTypeContrat());
        assertThrows(IllegalArgumentException.class, () -> r.definir(0, RegleConservation.SortFinal.DETRUIRE, "a", T0));
        assertThrows(IllegalArgumentException.class, () -> r.definir(RegleConservation.DUREE_MAX_ANS + 1, RegleConservation.SortFinal.DETRUIRE, "a", T0));
        assertThrows(IllegalArgumentException.class, () -> r.definir(3, null, "a", T0));
    }

    private static Trousse close() {
        Trousse t = new Trousse(7L, "TR-1", "Entente Agri-Nord", "init@iq.example", T0);
        t.setCopies("dg@iq.example");
        t.setValeursJson("{\"demandeur\":\"Agri-Nord\"}");
        t.changerStatut(StatutTrousse.ANNULEE, T0);
        return t;
    }

    @Test
    void la_cloture_date_la_conservation_et_seule_une_trousse_close_non_gelee_se_detruit() {
        Trousse brouillon = new Trousse(7L, "TR-0", "T", "i", T0);
        assertNull(brouillon.getClotureLe());
        assertThrows(IllegalStateException.class, () -> brouillon.detruire(T0), "non close");
        brouillon.changerStatut(StatutTrousse.EN_TRAITEMENT, T0);
        assertNull(brouillon.getClotureLe(), "en traitement : pas de clôture");
        brouillon.exigerOuverte();
        assertTrue(brouillon.enTraitement());

        Trousse t = close();
        assertEquals(T0, t.getClotureLe());
        assertThrows(IllegalStateException.class, t::exigerOuverte);
        assertThrows(IllegalArgumentException.class, () -> t.geler(" "));
        assertThrows(IllegalArgumentException.class, () -> t.geler(null));
        assertThrows(IllegalArgumentException.class, () -> t.geler("x".repeat(501)));
        assertThrows(IllegalStateException.class, t::leverGel, "aucun gel");
        t.geler(" Litige ");
        assertTrue(t.isGelLegal());
        assertEquals("Litige", t.getGelMotif());
        assertThrows(IllegalStateException.class, () -> t.detruire(T0));
        assertThrows(IllegalStateException.class, () -> t.marquerVersee(T0));
        t.leverGel();
        assertNull(t.getGelMotif());

        t.marquerVersee(T0);
        assertEquals(T0, t.getVerseeLe());
        t.detruire(T0);
        assertEquals(T0, t.getDetruiteLe());
        assertEquals("Trousse détruite — TR-1", t.getTitre());
        assertEquals("{}", t.getValeursJson());
        assertNull(t.getCopies());
        assertThrows(IllegalStateException.class, () -> t.detruire(T0), "déjà détruite");
        assertThrows(IllegalStateException.class, () -> t.geler("x"));
    }

    @Test
    void la_destruction_efface_contenus_coordonnees_et_donnees_de_signature() {
        DocumentTrousse d = new DocumentTrousse(7L, 1L, "c", 1, "a.pdf", FormatDocument.Type.PDF, "%PDF".getBytes(), "i", "m", T0);
        String empreinte = d.getEmpreinte();
        d.effacerContenu();
        assertEquals(0, d.getContenu().length);
        assertEquals(empreinte, d.getEmpreinte(), "l'empreinte demeure");

        Participant p = new Participant(7L, 1L, "Alice", "alice@exemple.ca", "+15145550101", "SIGNATAIRE_1", true, Identite.Niveau.QUESTION);
        ReflectionTestUtils.setField(p, "id", 9L);
        p.definirQuestion("Ville ?", "hash");
        p.anonymiser();
        assertEquals("Participant anonymisé", p.getNom());
        assertEquals("anonyme-9@supprime.invalid", p.getCourriel());
        assertNull(p.getTelephone());

        Invitation signee = new Invitation(7L, 1L, 2L, 9L, "h", T0.plusSeconds(86_400), Identite.Niveau.COURRIEL);
        signee.verifier(T0);
        signee.consentir(T0);
        signee.signer("Alice", "10.0.0.1", "Firefox", "{}", "{\"x\":1}", T0);
        signee.anonymiser();
        assertNull(signee.getNomSigne());
        assertEquals(Invitation.Statut.SIGNEE, signee.getStatut(), "le fait de la signature demeure");

        DecisionEtape decision = new DecisionEtape(7L, 1L, 2L, "v@iq.example", DecisionEtape.Nature.RETOUR, "commentaire libre", T0);
        decision.effacerCommentaire();
        assertNull(decision.getCommentaire());
    }

    @Test
    void un_nouveau_cycle_invalide_les_signatures_et_annule_les_invitations_ouvertes() {
        Invitation signee = new Invitation(7L, 1L, 2L, 9L, "h1", T0.plusSeconds(86_400), Identite.Niveau.COURRIEL);
        signee.verifier(T0);
        signee.consentir(T0);
        signee.signer("Alice", null, null, "{}", "{}", T0);
        signee.invalider();
        assertEquals(Invitation.Statut.INVALIDEE, signee.getStatut());

        Invitation ouverte = new Invitation(7L, 1L, 2L, 8L, "h2", T0.plusSeconds(86_400), Identite.Niveau.COURRIEL);
        ouverte.invalider();
        assertEquals(Invitation.Statut.ANNULEE, ouverte.getStatut());
    }
}
