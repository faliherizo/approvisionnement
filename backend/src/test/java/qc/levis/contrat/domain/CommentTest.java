package qc.levis.contrat.domain;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.redaction.domain.Comment;
import static org.junit.jupiter.api.Assertions.*;

/** Tests unitaires des accesseurs de l'entité {@link Comment} (dont résolution et fil). */
class CommentTest {

    @Test
    void accesseurs_couvrentTousLesChamps() {
        Comment c = new Comment();
        Instant t = Instant.now();
        c.setDocumentId(3L);
        c.setClauseId(9L);
        c.setParentId(4L);
        c.setAuteur("alice");
        c.setCorps("à revoir");
        c.setResolu(true);
        c.setResoluPar("bob");
        c.setCreeLe(t);

        assertNull(c.getId());
        assertEquals(3L, c.getDocumentId());
        assertEquals(9L, c.getClauseId());
        assertEquals(4L, c.getParentId());
        assertEquals("alice", c.getAuteur());
        assertEquals("à revoir", c.getCorps());
        assertTrue(c.isResolu());
        assertEquals("bob", c.getResoluPar());
        assertEquals(t, c.getCreeLe());
    }
}
