package qc.levis.contrat.catalogue.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.catalogue.domain.Application;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByTenantIdOrderByNomAsc(Long tenantId);
    List<Application> findByTenantIdAndDomaineOrderByNomAsc(Long tenantId, String domaine);
    Optional<Application> findByIdAndTenantId(Long id, Long tenantId);
    boolean existsByTenantIdAndCode(Long tenantId, String code);
}
