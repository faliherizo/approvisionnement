package qc.levis.contrat.domain;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.commun.domain.Enums;
import qc.levis.contrat.integration.domain.PublicationLog;
import static org.junit.jupiter.api.Assertions.*;

/** Tests unitaires des accesseurs de {@link PublicationLog}. */
class PublicationLogTest {

    @Test
    void accesseurs_couvrentTousLesChamps() {
        PublicationLog p = new PublicationLog();
        Instant t = Instant.now();
        p.setDocumentId(3L);
        p.setCible(Enums.CiblePublication.TEAMS);
        p.setSucces(false);
        p.setUrlCible("sim://teams/x.pdf");
        p.setMessage("échec simulé");
        p.setPubliePar("alice");
        p.setPublieLe(t);
        p.setTenantId(7L);

        assertNull(p.getId());
        assertEquals(3L, p.getDocumentId());
        assertEquals(Enums.CiblePublication.TEAMS, p.getCible());
        assertFalse(p.isSucces());
        assertEquals("sim://teams/x.pdf", p.getUrlCible());
        assertEquals("échec simulé", p.getMessage());
        assertEquals("alice", p.getPubliePar());
        assertEquals(t, p.getPublieLe());
        assertEquals(7L, p.getTenantId());
    }
}
