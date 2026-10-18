package qc.levis.contrat.ia.application.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.ia.domain.KnowledgeSnippet;

public interface KnowledgeRepository extends JpaRepository<KnowledgeSnippet, Long> {
    List<KnowledgeSnippet> findByTenantId(Long tenantId);
    List<KnowledgeSnippet> findByTenantIdIsNull();
    long countByTenantIdIsNull();
}
