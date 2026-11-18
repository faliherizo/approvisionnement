package qc.levis.contrat.clm.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.clm.domain.GroupeClm;

public interface GroupeClmRepository extends JpaRepository<GroupeClm, Long> {
    List<GroupeClm> findByTenantIdOrderByCodeAsc(Long tenantId);

    Optional<GroupeClm> findByIdAndTenantId(Long id, Long tenantId);

    boolean existsByTenantIdAndCode(Long tenantId, String code);

    Optional<GroupeClm> findByTenantIdAndCode(Long tenantId, String code);
}
