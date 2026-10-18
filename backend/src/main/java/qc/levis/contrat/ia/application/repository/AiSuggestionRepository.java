package qc.levis.contrat.ia.application.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.ia.domain.AiSuggestion;

public interface AiSuggestionRepository extends JpaRepository<AiSuggestion, Long> {
    List<AiSuggestion> findByTenantIdOrderByCreeLeDesc(Long tenantId);
}
