package qc.levis.contrat.messagerie.application.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.messagerie.domain.Lecture;

public interface LectureRepository extends JpaRepository<Lecture, Long> {
    Optional<Lecture> findByConversationIdAndParticipant(Long conversationId, String participant);
}
