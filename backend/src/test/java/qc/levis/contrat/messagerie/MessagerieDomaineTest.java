package qc.levis.contrat.messagerie;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.messagerie.domain.Conversation;
import qc.levis.contrat.messagerie.domain.Lecture;
import qc.levis.contrat.messagerie.domain.Message;

class MessagerieDomaineTest {

    static final Instant T0 = Instant.parse("2026-11-20T10:00:00Z");

    @Test
    void la_conversation_n_existe_que_pour_ses_participants() {
        Conversation c = new Conversation(1L, "  Dossier RH-12 ", " ", "Alice@chu.example", T0);
        assertEquals("Dossier RH-12", c.getSujet());
        assertNull(c.getReference());
        assertTrue(c.estParticipant("ALICE@chu.example"));
        c.ajouterParticipant(" Bob@CHU.example ");
        assertTrue(c.estParticipant("bob@chu.example"));
        assertFalse(c.estParticipant(null));
        assertThrows(NoSuchElementException.class, () -> c.exigerParticipant("eve@chu.example"));
        assertDoesNotThrow(() -> c.exigerParticipant("bob@chu.example"));
        assertThrows(IllegalArgumentException.class, () -> new Conversation(1L, " ", null, "a@b.ca", T0));
        assertEquals(new Conversation(1L, "S", "RH-1", "a@b.ca", T0).getReference(), "RH-1");
    }

    @Test
    void une_conversation_compte_au_plus_vingt_participants() {
        Conversation c = new Conversation(1L, "S", null, "p0@x.ca", T0);
        for (int i = 1; i < Conversation.PARTICIPANTS_MAX; i++) c.ajouterParticipant("p" + i + "@x.ca");
        assertThrows(IllegalArgumentException.class, () -> c.ajouterParticipant("p99@x.ca"));
        assertDoesNotThrow(() -> c.ajouterParticipant("p1@x.ca"), "réajouter un participant existant est sans effet");
        assertThrows(UnsupportedOperationException.class, () -> c.getParticipants().clear());
    }

    @Test
    void la_pseudonymisation_retire_le_participant_et_le_createur() {
        Conversation c = new Conversation(1L, "S", null, "alice@x.ca", T0);
        c.ajouterParticipant("bob@x.ca");
        c.pseudonymiserParticipant("Alice@x.ca", "personne-effacee-1");
        assertFalse(c.estParticipant("alice@x.ca"));
        assertTrue(c.getParticipants().contains("personne-effacee-1"));
        assertEquals("personne-effacee-1", c.getCreePar());
        c.pseudonymiserParticipant("eve@x.ca", "personne-effacee-2");
        assertFalse(c.getParticipants().contains("personne-effacee-2"));
        c.messageRecu(T0.plusSeconds(5));
        assertEquals(T0.plusSeconds(5), c.getDernierMessageLe());
        assertEquals(T0, c.getCreeLe());
        assertEquals(1L, c.getTenantId());
    }

    @Test
    void un_message_est_du_texte_borne_et_la_lecture_n_avance_que_vers_l_avant() {
        assertEquals("Bonjour", Message.exigerTexte("  Bonjour \n"));
        assertThrows(IllegalArgumentException.class, () -> Message.exigerTexte(" "));
        assertThrows(IllegalArgumentException.class, () -> Message.exigerTexte(null));
        assertThrows(IllegalArgumentException.class, () -> Message.exigerTexte("x".repeat(Message.LONGUEUR_MAX + 1)));
        Message m = new Message(3L, "Alice@x.ca", "chiffre", T0);
        assertEquals("alice@x.ca", m.getAuteur());
        m.pseudonymiserAuteur("personne-effacee-1");
        assertEquals("personne-effacee-1", m.getAuteur());
        assertEquals(java.util.List.of(3L, "chiffre", T0), java.util.List.of(m.getConversationId(), m.getContenuChiffre(), m.getEnvoyeLe()));
        Lecture l = new Lecture(3L, "Bob@x.ca", T0);
        l.avancer(T0.minusSeconds(60));
        assertEquals(T0, l.getLuJusqua());
        l.avancer(T0.plusSeconds(60));
        assertEquals(T0.plusSeconds(60), l.getLuJusqua());
        assertEquals("bob@x.ca", l.getParticipant());
        assertEquals(3L, l.getConversationId());
    }

}
