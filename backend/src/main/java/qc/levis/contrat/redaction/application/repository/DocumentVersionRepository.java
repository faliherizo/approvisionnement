package qc.levis.contrat.redaction.application.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.redaction.domain.DocumentVersion;

public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, Long> {
    List<DocumentVersion> findByDocumentIdOrderByNumeroVersionAsc(Long documentId);
}
