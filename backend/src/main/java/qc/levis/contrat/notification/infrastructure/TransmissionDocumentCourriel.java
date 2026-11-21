package qc.levis.contrat.notification.infrastructure;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import qc.levis.contrat.notification.application.repository.NotificationEnvoiRepository;
import qc.levis.contrat.notification.domain.NotificationEnvoi;
import qc.levis.contrat.saisie.application.port.TransmissionDocument;

/**
 * Transmission d'un document par courriel, en pièce jointe (CHU 5.04). En mode SMTP, le relais de l'organisation remet le
 * message ; sinon l'envoi est simulé. Dans les deux cas il est inscrit au journal des notifications (sans le document).
 */
@Component
class TransmissionDocumentCourriel implements TransmissionDocument {

    private static final Logger log = LoggerFactory.getLogger(TransmissionDocumentCourriel.class);

    private final ObjectProvider<JavaMailSender> expediteur;
    private final NotificationEnvoiRepository journal;
    private final boolean smtp;
    private final String de;

    TransmissionDocumentCourriel(ObjectProvider<JavaMailSender> expediteur, NotificationEnvoiRepository journal,
                                 @Value("${app.notification.courriel.mode:simulation}") String mode,
                                 @Value("${app.notification.courriel.expediteur}") String de) {
        this.expediteur = expediteur;
        this.journal = journal;
        this.smtp = "smtp".equals(mode);
        this.de = de;
    }

    @Override
    public String transmettre(Long tenantId, String destinataire, String sujet, String message, String nomFichier, byte[] contenu) {
        String statut = smtp ? envoyer(destinataire, sujet, message, nomFichier, contenu) : "SIMULE";
        journal.save(new NotificationEnvoi(tenantId, "COURRIEL", destinataire, sujet, message + "\n[pièce jointe : " + nomFichier + "]", statut));
        return statut;
    }

    private String envoyer(String destinataire, String sujet, String message, String nomFichier, byte[] contenu) {
        try {
            JavaMailSender mail = expediteur.getObject();
            MimeMessage m = mail.createMimeMessage();
            MimeMessageHelper h = new MimeMessageHelper(m, true, "UTF-8");
            h.setFrom(de);
            h.setTo(destinataire);
            h.setSubject(sujet);
            h.setText(message, false);
            h.addAttachment(nomFichier, new ByteArrayResource(contenu), "application/pdf");
            mail.send(m);
            return "ENVOYE";
        } catch (MessagingException | MailException e) {
            log.warn("Document non remis (domaine {}) : {}", CourrielSmtp.domaine(destinataire), e.getMessage());
            return "ECHEC";
        }
    }
}
