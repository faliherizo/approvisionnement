package qc.levis.contrat.approbation.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.approbation.domain.ApprovalWorkflow;
import qc.levis.contrat.commun.domain.Enums;

public interface ApprovalWorkflowRepository extends JpaRepository<ApprovalWorkflow, Long> {
    List<ApprovalWorkflow> findByTenantId(Long tenantId);
    Optional<ApprovalWorkflow> findFirstByTenantIdAndTypeAndActifTrue(Long tenantId, Enums.TypeContrat type);
}
