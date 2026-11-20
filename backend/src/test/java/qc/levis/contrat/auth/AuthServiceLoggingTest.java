package qc.levis.contrat.auth;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import qc.levis.contrat.audit.application.AuditService;
import qc.levis.contrat.auth.application.AuthService;
import qc.levis.contrat.auth.application.JwtService;
import qc.levis.contrat.auth.application.TotpService;
import qc.levis.contrat.auth.application.repository.UserRepository;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Vérifie la politique de journalisation de l'authentification : un échec de connexion journalise
 * l'événement (courriel masqué) mais JAMAIS le mot de passe (données sensibles — Loi 25).
 */
class AuthServiceLoggingTest {

    private final ch.qos.logback.classic.Logger logger =
            (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(AuthService.class);
    private final ListAppender<ILoggingEvent> appender = new ListAppender<>();

    private UserRepository users;
    private AuthService service;

    @BeforeEach
    void setUp() {
        appender.start();
        logger.addAppender(appender);
        users = mock(UserRepository.class);
        service = new AuthService(users, mock(org.springframework.security.crypto.password.PasswordEncoder.class),
                mock(JwtService.class), mock(TotpService.class), mock(AuditService.class), mock(qc.levis.contrat.auth.application.repository.PolitiqueConnexionRepository.class));
    }

    @AfterEach
    void tearDown() { logger.detachAppender(appender); }

    @Test
    void echecConnexion_journaliseSansLeMotDePasse() {
        when(users.findByEmail("jean.tremblay@ville.qc.ca")).thenReturn(Optional.empty());

        service.authentifier("jean.tremblay@ville.qc.ca", "MotDePasseTresSecret!", null, null);

        List<ILoggingEvent> events = appender.list;
        // L'échec est bien tracé, au niveau WARN...
        assertTrue(events.stream().anyMatch(e -> e.getLevel() == Level.WARN));
        // ...avec le courriel MASQUÉ (jamais l'adresse complète)...
        assertTrue(events.stream().anyMatch(e -> e.getFormattedMessage().contains("j***@ville.qc.ca")));
        assertTrue(events.stream().noneMatch(e -> e.getFormattedMessage().contains("jean.tremblay@ville.qc.ca")));
        // ...et JAMAIS le mot de passe.
        assertTrue(events.stream().noneMatch(e -> e.getFormattedMessage().contains("MotDePasseTresSecret")));
    }
}
