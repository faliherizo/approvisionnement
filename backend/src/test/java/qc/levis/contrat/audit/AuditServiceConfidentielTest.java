package qc.levis.contrat.audit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.LoggerFactory;
import qc.levis.contrat.audit.application.AuditService;
import qc.levis.contrat.audit.application.repository.AuditLogRepository;
import qc.levis.contrat.audit.domain.AuditLog;

/**
 * Valeurs d'affaires au journal (Annexe 10, SEC-15) : le détail avant/après est conservé au journal d'audit, qui fait
 * preuve, mais n'atteint jamais le journal technique (Loi 25, docs/journalisation.md).
 */
class AuditServiceConfidentielTest {

    private final ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(AuditService.class);
    private final ListAppender<ILoggingEvent> appender = new ListAppender<>();
    private final AuditLogRepository repo = mock(AuditLogRepository.class);
    private final AuditService audit = new AuditService(repo);

    @BeforeEach
    void brancher() {
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void debrancher() {
        logger.detachAppender(appender);
    }

    @Test
    void le_detail_va_au_journal_d_audit_mais_pas_au_journal_technique() {
        audit.logConfidentiel(7L, "camille.tremblay@iq.example", "CLM_VALEUR_MODIFIEE", "TROUSSE", 3L,
                "TR-1 · « NAS » (nas) : « 123 456 789 » → « 987 654 321 »");

        ArgumentCaptor<AuditLog> entree = ArgumentCaptor.forClass(AuditLog.class);
        verify(repo).save(entree.capture());
        assertTrue(entree.getValue().getDetails().contains("987 654 321"), "la preuve est complète en base");
        assertEquals(7L, entree.getValue().getTenantId());
        String technique = appender.list.get(0).getFormattedMessage();
        assertFalse(technique.contains("987") || technique.contains("123 456"), technique);
        assertFalse(technique.contains("camille.tremblay@iq.example"), "courriel masqué");
        assertTrue(technique.contains("CLM_VALEUR_MODIFIEE") && technique.contains("détail au journal d'audit"), technique);
    }
}
