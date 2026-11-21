package qc.levis.contrat.messagerie.infrastructure.vieprivee;

import java.util.List;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;
import qc.levis.contrat.commun.application.SecretCipher;
import qc.levis.contrat.messagerie.application.repository.ConversationRepository;
import qc.levis.contrat.messagerie.application.repository.MessageRepository;
import qc.levis.contrat.messagerie.domain.Conversation;
import qc.levis.contrat.messagerie.domain.Message;
import qc.levis.contrat.vieprivee.application.port.SourceRenseignementsPersonnels;
import qc.levis.contrat.vieprivee.domain.BilanEffacement;
import qc.levis.contrat.vieprivee.domain.ExtraitRenseignements;
import qc.levis.contrat.vieprivee.domain.Pseudonyme;
import qc.levis.contrat.vieprivee.domain.Renseignement;

/** Messagerie interne : conversations auxquelles la personne participe et messages qu'elle a écrits (en clair). */
@Component
class MessagerieRenseignements implements SourceRenseignementsPersonnels {

    private final ConversationRepository conversations;
    private final MessageRepository messages;
    private final SecretCipher chiffrement;

    MessagerieRenseignements(ConversationRepository conversations, MessageRepository messages, SecretCipher chiffrement) {
        this.conversations = conversations;
        this.messages = messages;
        this.chiffrement = chiffrement;
    }

    private List<Conversation> siennes(Long tenantId, String courriel) {
        return conversations.deLaPersonne(tenantId, Conversation.normaliser(courriel));
    }

    @Override
    public ExtraitRenseignements extraire(Long tenantId, String courriel) {
        List<Conversation> siennes = siennes(tenantId, courriel);
        List<Long> ids = siennes.stream().map(Conversation::getId).toList();
        Stream<Renseignement> fils = siennes.stream().map(c -> Renseignement.de("conversation/" + c.getId(), c.getCreeLe(), "sujet", c.getSujet()));
        Stream<Renseignement> ecrits = messages.findByAuteur(Conversation.normaliser(courriel)).stream()
                .filter(m -> ids.contains(m.getConversationId()))
                .map(m -> Renseignement.de("message/" + m.getId(), m.getEnvoyeLe(), "texte", chiffrement.dechiffrer(m.getContenuChiffre())));
        return new ExtraitRenseignements("Messagerie", "Conversations entre intervenants et messages écrits par la personne.",
                "Les messages restent dans les conversations des autres participants ; leur auteur devient un pseudonyme.",
                Stream.concat(fils, ecrits).toList());
    }

    @Override
    public BilanEffacement effacer(Long tenantId, String courriel, Pseudonyme pseudonyme) {
        List<Conversation> siennes = siennes(tenantId, courriel);
        List<Long> ids = siennes.stream().map(Conversation::getId).toList();
        List<Message> ecrits = messages.findByAuteur(Conversation.normaliser(courriel)).stream()
                .filter(m -> ids.contains(m.getConversationId())).toList();
        ecrits.forEach(m -> m.pseudonymiserAuteur(pseudonyme.valeur()));
        messages.saveAll(ecrits);
        siennes.forEach(c -> c.pseudonymiserParticipant(courriel, pseudonyme.valeur()));
        conversations.saveAll(siennes);
        return BilanEffacement.complet("Messagerie", siennes.size() + ecrits.size());
    }
}
