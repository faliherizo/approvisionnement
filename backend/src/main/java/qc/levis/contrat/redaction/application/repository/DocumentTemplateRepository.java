package qc.levis.contrat.redaction.application.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.redaction.domain.DocumentTemplate;

public interface DocumentTemplateRepository extends JpaRepository<DocumentTemplate, Long> {
    java.util.List<DocumentTemplate> findByTenantId(Long tenantId);
}
