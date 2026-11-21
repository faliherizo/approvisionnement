package qc.levis.contrat.messagerie.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import qc.levis.contrat.messagerie.domain.Conversation;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    Optional<Conversation> findByIdAndTenantId(Long id, Long tenantId);

    @Query("select c from Conversation c join c.participants p where c.tenantId = :tenantId and p = :courriel order by c.dernierMessageLe desc")
    List<Conversation> deLaPersonne(@Param("tenantId") Long tenantId, @Param("courriel") String courriel);
}
