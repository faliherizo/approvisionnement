package qc.levis.contrat.securite.application.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.securite.domain.PermissionAcces;

public interface PermissionAccesRepository extends JpaRepository<PermissionAcces, Long> {
    List<PermissionAcces> findByTenantId(Long tenantId);
}
