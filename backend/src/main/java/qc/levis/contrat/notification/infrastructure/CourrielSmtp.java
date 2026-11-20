package qc.levis.contrat.notification.infrastructure;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import qc.levis.contrat.notification.application.port.CanalNotification;

/**
 * Canal COURRIEL par SMTP (ADR-002) : serveur de l'organisation ou relais (Azure Communication Services, Microsoft 365).
 * L'expéditeur est celui du domaine configuré (SPF, DKIM : question H2). Le destinataire n'est jamais journalisé en
 * clair (Loi 25) ; un échec rend « ECHEC », que la messagerie consigne et relance.
 */
@Component
@ConditionalOnProperty(name = "app.notification.courriel.mode", havingValue = "smtp")
public class CourrielSmtp implements CanalNotification {

    private static final Logger log = LoggerFactory.getLogger(CourrielSmtp.class);

    private final JavaMailSender expediteur;
    private final String de;

    public CourrielSmtp(JavaMailSender expediteur, @Value("${app.notification.courriel.expediteur}") String de) {
        this.expediteur = expediteur;
        this.de = de;
    }

    @Override
    public String canal() {
        return "COURRIEL";
    }

    @Override
    public String envoyer(String destinataire, String sujet, String corps) {
        try {
            MimeMessage m = expediteur.createMimeMessage();
            MimeMessageHelper h = new MimeMessageHelper(m, false, "UTF-8");
            h.setFrom(de);
            h.setTo(destinataire);
            h.setSubject(sujet);
            h.setText(corps, false);
            expediteur.send(m);
            return "ENVOYE";
        } catch (MessagingException | MailException e) {
            log.warn("Courriel non remis (domaine {}) : {}", domaine(destinataire), e.getMessage());
            return "ECHEC";
        }
    }

    static String domaine(String courriel) {
        int a = courriel == null ? -1 : courriel.indexOf('@');
        return a < 0 ? "?" : courriel.substring(a + 1);
    }
}
