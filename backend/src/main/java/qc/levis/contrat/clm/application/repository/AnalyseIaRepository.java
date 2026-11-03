package qc.levis.contrat.clm.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.clm.domain.AnalyseIa;

public interface AnalyseIaRepository extends JpaRepository<AnalyseIa, Long> {
    Optional<AnalyseIa> findByIdAndTenantId(Long id, Long tenantId);

    List<AnalyseIa> findByTenantIdAndTrousseIdOrderByCreeLeDesc(Long tenantId, Long trousseId);

    List<AnalyseIa> findTop100ByTenantIdOrderByCreeLeDesc(Long tenantId);
}
