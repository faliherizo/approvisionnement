package qc.levis.contrat.redaction.application.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.redaction.domain.Obligation;

public interface ObligationRepository extends JpaRepository<Obligation, Long> {
    List<Obligation> findByDocumentIdOrderByDateEcheanceAsc(Long documentId);
    List<Obligation> findByDateEcheanceLessThanEqualOrderByDateEcheanceAsc(LocalDate limite);
    List<Obligation> findByTenantIdAndDateEcheanceLessThanEqualOrderByDateEcheanceAsc(
            Long tenantId, LocalDate limite);
}
