package qc.levis.contrat.domain;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.ia.domain.AiSuggestion;
import static org.junit.jupiter.api.Assertions.*;

/** Tests unitaires des accesseurs de {@link AiSuggestion} (constructeur + setters). */
class AiSuggestionTest {

    @Test
    void constructeur_etAccesseurs() {
        AiSuggestion s = new AiSuggestion("CHAT", "invite", "reponse", "llama3.1", "alice", 7L);
        assertEquals("CHAT", s.getTypeInteraction());
        assertEquals("invite", s.getInvite());
        assertEquals("reponse", s.getReponse());
        assertEquals("llama3.1", s.getModele());
        assertEquals("alice", s.getUtilisateur());
        assertEquals(7L, s.getTenantId());
        assertNull(s.getId());
        assertNotNull(s.getCreeLe());
    }

    @Test
    void settersParDefaut() {
        AiSuggestion s = new AiSuggestion();
        Instant t = Instant.now();
        s.setTypeInteraction("REDIGER");
        s.setInvite("i");
        s.setReponse("r");
        s.setModele("m");
        s.setUtilisateur("u");
        s.setCreeLe(t);
        s.setTenantId(3L);
        assertEquals("REDIGER", s.getTypeInteraction());
        assertEquals("i", s.getInvite());
        assertEquals("r", s.getReponse());
        assertEquals("m", s.getModele());
        assertEquals("u", s.getUtilisateur());
        assertEquals(t, s.getCreeLe());
        assertEquals(3L, s.getTenantId());
    }
}
