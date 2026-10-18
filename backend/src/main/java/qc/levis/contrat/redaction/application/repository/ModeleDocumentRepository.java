package qc.levis.contrat.redaction.application.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.redaction.domain.ModeleDocument;

public interface ModeleDocumentRepository extends JpaRepository<ModeleDocument, Long> {
    Optional<ModeleDocument> findByTenantId(Long tenantId);
    Optional<ModeleDocument> findByTenantIdIsNull();
}
