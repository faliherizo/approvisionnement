package qc.levis.contrat.integration.application.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.integration.domain.PublicationLog;

public interface PublicationLogRepository extends JpaRepository<PublicationLog, Long> {
    List<PublicationLog> findByDocumentIdOrderByPublieLeDesc(Long documentId);
}
