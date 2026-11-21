package qc.levis.contrat.messagerie.application.repository;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.messagerie.domain.Message;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByConversationIdOrderByEnvoyeLeAscIdAsc(Long conversationId);
    List<Message> findByAuteur(String auteur);
    long countByConversationIdAndEnvoyeLeAfterAndAuteurNot(Long conversationId, Instant apres, String auteur);
}
