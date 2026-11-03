package qc.levis.contrat.processus.application.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.processus.domain.Workflow;

public interface WorkflowRepository extends JpaRepository<Workflow, Long> {
    Optional<Workflow> findByTenantIdAndFormulaireId(Long tenantId, Long formulaireId);
}
