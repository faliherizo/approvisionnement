package qc.levis.contrat.commun.module.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.commun.module.domain.TenantModule;

public interface TenantModuleRepository extends JpaRepository<TenantModule, Long> {
    List<TenantModule> findByTenantId(Long tenantId);
    Optional<TenantModule> findByTenantIdAndModuleCode(Long tenantId, String moduleCode);
}
