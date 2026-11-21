package qc.levis.contrat.notification.infrastructure;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import qc.levis.contrat.notification.application.repository.NotificationEnvoiRepository;
import qc.levis.contrat.notification.domain.NotificationEnvoi;

/** CHU 5.04 : le document part en pièce jointe par le relais SMTP ; tout envoi est inscrit au journal, sans le document. */
class TransmissionDocumentCourrielTest {

    JavaMailSender mail = mock(JavaMailSender.class);
    NotificationEnvoiRepository journal = mock(NotificationEnvoiRepository.class);
    @SuppressWarnings("unchecked") ObjectProvider<JavaMailSender> fournisseur = mock(ObjectProvider.class);

    TransmissionDocumentCourriel en(String mode) {
        when(fournisseur.getObject()).thenReturn(mail);
        when(mail.createMimeMessage()).thenReturn(new MimeMessage(Session.getInstance(new Properties())));
        return new TransmissionDocumentCourriel(fournisseur, journal, mode, "no-reply@chu.example");
    }

    @Test
    void en_smtp_le_document_part_en_piece_jointe_et_l_envoi_est_journalise() throws Exception {
        assertEquals("ENVOYE", en("smtp").transmettre(3L, "partenaire@chu.example", "Fiche RH-1", "Voici", "RH-1.pdf", "%PDF".getBytes()));
        ArgumentCaptor<MimeMessage> m = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mail).send(m.capture());
        MimeMultipart corps = (MimeMultipart) m.getValue().getContent();
        boolean pdf = false;
        for (int i = 0; i < corps.getCount(); i++) pdf |= "RH-1.pdf".equals(corps.getBodyPart(i).getFileName());
        assertTrue(pdf, "pièce jointe RH-1.pdf");
        ArgumentCaptor<NotificationEnvoi> j = ArgumentCaptor.forClass(NotificationEnvoi.class);
        verify(journal).save(j.capture());
        assertEquals("ENVOYE", j.getValue().getStatut());
        assertTrue(j.getValue().getCorps().contains("[pièce jointe : RH-1.pdf]"));
    }

    @Test
    void un_refus_du_relais_est_un_echec_journalise() {
        TransmissionDocumentCourriel t = en("smtp");
        doThrow(new MailSendException("refusé")).when(mail).send(any(MimeMessage.class));
        assertEquals("ECHEC", t.transmettre(3L, "x@chu.example", "S", "M", "a.pdf", new byte[1]));
        verify(journal).save(argThat((NotificationEnvoi n) -> "ECHEC".equals(n.getStatut())));
    }

    @Test
    void hors_smtp_l_envoi_est_simule_sans_toucher_au_relais() {
        assertEquals("SIMULE", en("simulation").transmettre(3L, "x@chu.example", "S", "M", "a.pdf", new byte[1]));
        verify(mail, never()).send(any(MimeMessage.class));
        verify(journal).save(any());
    }
}
