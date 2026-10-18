package qc.levis.contrat.redaction.application.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.redaction.domain.Document;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByCreePar(String creePar);
    List<Document> findByTenantId(Long tenantId);
    List<Document> findByTenantIdAndActifTrue(Long tenantId);
    List<Document> findByDossierIdAndActifTrue(Long dossierId);
    long countByDossierIdAndActifTrue(Long dossierId);
}
