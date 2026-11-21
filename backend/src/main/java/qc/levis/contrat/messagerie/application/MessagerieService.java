package qc.levis.contrat.messagerie.application;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.audit.application.AuditService;
import qc.levis.contrat.commun.application.SecretCipher;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.messagerie.application.dto.ConversationDetail;
import qc.levis.contrat.messagerie.application.dto.ConversationResume;
import qc.levis.contrat.messagerie.application.dto.NouvelleConversationRequest;
import qc.levis.contrat.messagerie.application.port.AnnuaireMembres;
import qc.levis.contrat.messagerie.application.repository.ConversationRepository;
import qc.levis.contrat.messagerie.application.repository.LectureRepository;
import qc.levis.contrat.messagerie.application.repository.MessageRepository;
import qc.levis.contrat.messagerie.domain.Conversation;
import qc.levis.contrat.messagerie.domain.Lecture;
import qc.levis.contrat.messagerie.domain.Message;

/**
 * Messagerie interne sécurisée (CHU 5.05) : conversations entre membres actifs d'une organisation, messages chiffrés au
 * repos, visibles des seuls participants. Le journal d'audit trace les échanges sans leur contenu.
 */
@Service
public class MessagerieService {

    private final ConversationRepository conversations;
    private final MessageRepository messages;
    private final LectureRepository lectures;
    private final AnnuaireMembres annuaire;
    private final SecretCipher chiffrement;
    private final AuditService audit;
    private final Clock horloge;

    public MessagerieService(ConversationRepository conversations, MessageRepository messages, LectureRepository lectures,
                             AnnuaireMembres annuaire, SecretCipher chiffrement, AuditService audit, Clock horloge) {
        this.conversations = conversations;
        this.messages = messages;
        this.lectures = lectures;
        this.annuaire = annuaire;
        this.chiffrement = chiffrement;
        this.audit = audit;
        this.horloge = horloge;
    }

    public List<ConversationResume> mesConversations(String moi) {
        return conversations.deLaPersonne(TenantContext.get(), Conversation.normaliser(moi)).stream()
                .map(c -> new ConversationResume(c.getId(), c.getSujet(), c.getReference(), c.getParticipants(),
                        c.getDernierMessageLe(), nonLus(c, moi)))
                .toList();
    }

    @Transactional
    public ConversationDetail creer(NouvelleConversationRequest req, String moi) {
        Instant le = horloge.instant();
        Conversation c = new Conversation(TenantContext.get(), req.sujet(), req.reference(), moi, le);
        for (String p : req.participants()) c.ajouterParticipant(exigerMembre(p));
        c = conversations.save(c);
        ecrire(c, req.message(), moi, le);
        audit.log(moi, "MESSAGERIE_CONVERSATION", "Conversation", c.getId(), c.getParticipants().size() + " participant(s)");
        return ouvrir(c.getId(), moi);
    }

    /** Ouvrir la conversation marque ses messages comme lus pour la personne. */
    @Transactional
    public ConversationDetail ouvrir(Long id, String moi) {
        Conversation c = exigerParticipation(id, moi);
        List<ConversationDetail.MessageVue> fil = messages.findByConversationIdOrderByEnvoyeLeAscIdAsc(id).stream()
                .map(m -> new ConversationDetail.MessageVue(m.getId(), m.getAuteur(), chiffrement.dechiffrer(m.getContenuChiffre()), m.getEnvoyeLe()))
                .toList();
        marquerLu(c, moi, horloge.instant());
        return new ConversationDetail(c.getId(), c.getSujet(), c.getReference(), c.getParticipants(), fil);
    }

    @Transactional
    public ConversationDetail repondre(Long id, String texte, String moi) {
        Conversation c = exigerParticipation(id, moi);
        ecrire(c, texte, moi, horloge.instant());
        audit.log(moi, "MESSAGERIE_MESSAGE", "Conversation", id, null);
        return ouvrir(id, moi);
    }

    @Transactional
    public ConversationDetail ajouterParticipant(Long id, String courriel, String moi) {
        Conversation c = exigerParticipation(id, moi);
        c.ajouterParticipant(exigerMembre(courriel));
        conversations.save(c);
        audit.log(moi, "MESSAGERIE_PARTICIPANT", "Conversation", id, null);
        return ouvrir(id, moi);
    }

    private void ecrire(Conversation c, String texte, String auteur, Instant le) {
        messages.save(new Message(c.getId(), auteur, chiffrement.chiffrer(Message.exigerTexte(texte)), le));
        c.messageRecu(le);
        conversations.save(c);
        marquerLu(c, auteur, le);
    }

    private Conversation exigerParticipation(Long id, String moi) {
        Conversation c = conversations.findByIdAndTenantId(id, TenantContext.get())
                .orElseThrow(() -> new NoSuchElementException("Conversation introuvable : " + id));
        c.exigerParticipant(moi);
        return c;
    }

    private String exigerMembre(String courriel) {
        if (!annuaire.estMembreActif(TenantContext.get(), Conversation.normaliser(courriel))) {
            throw new IllegalArgumentException("« " + courriel + " » n'est pas un membre actif de l'organisation.");
        }
        return courriel;
    }

    private long nonLus(Conversation c, String moi) {
        Instant depuis = lectures.findByConversationIdAndParticipant(c.getId(), Conversation.normaliser(moi))
                .map(Lecture::getLuJusqua).orElse(Instant.EPOCH);
        return messages.countByConversationIdAndEnvoyeLeAfterAndAuteurNot(c.getId(), depuis, Conversation.normaliser(moi));
    }

    private void marquerLu(Conversation c, String moi, Instant le) {
        Lecture l = lectures.findByConversationIdAndParticipant(c.getId(), Conversation.normaliser(moi))
                .orElseGet(() -> new Lecture(c.getId(), moi, le));
        l.avancer(le);
        lectures.save(l);
    }
}
