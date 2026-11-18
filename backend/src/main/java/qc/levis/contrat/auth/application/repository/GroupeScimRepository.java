package qc.levis.contrat.auth.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.auth.domain.GroupeScim;

public interface GroupeScimRepository extends JpaRepository<GroupeScim, String> {
    Optional<GroupeScim> findByIdAndTenantId(String id, Long tenantId);

    List<GroupeScim> findByTenantIdOrderByNomAsc(Long tenantId);

    List<GroupeScim> findByTenantIdAndIdIn(Long tenantId, java.util.Collection<String> ids);

    boolean existsByTenantIdAndNomIgnoreCase(Long tenantId, String nom);
}
