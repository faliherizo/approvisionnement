package qc.levis.contrat.interop.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.interop.domain.EchangeHerite;

public interface EchangeHeriteRepository extends JpaRepository<EchangeHerite, Long> {
    List<EchangeHerite> findByTenantIdOrderByCodeAsc(Long tenantId);
    Optional<EchangeHerite> findByIdAndTenantId(Long id, Long tenantId);
    Optional<EchangeHerite> findByTenantIdAndCodeAndActifTrue(Long tenantId, String code);
    boolean existsByTenantIdAndCode(Long tenantId, String code);
}
