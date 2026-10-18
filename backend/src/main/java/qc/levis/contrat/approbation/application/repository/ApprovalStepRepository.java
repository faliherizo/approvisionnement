package qc.levis.contrat.approbation.application.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.approbation.domain.ApprovalStep;

public interface ApprovalStepRepository extends JpaRepository<ApprovalStep, Long> {
    List<ApprovalStep> findByDocumentIdOrderByNiveauAsc(Long documentId);
}
