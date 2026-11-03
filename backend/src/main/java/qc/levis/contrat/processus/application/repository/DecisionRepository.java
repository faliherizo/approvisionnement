package qc.levis.contrat.processus.application.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.processus.domain.Decision;

public interface DecisionRepository extends JpaRepository<Decision, Long> {
    List<Decision> findByTenantIdAndSaisieIdOrderByDecideLeAsc(Long tenantId, Long saisieId);
}
