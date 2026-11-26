package qc.levis.contrat.clm.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.clm.domain.CleOrganisation;

public interface CleOrganisationRepository extends JpaRepository<CleOrganisation, Long> {
    Optional<CleOrganisation> findByTenantIdAndStatut(Long tenantId, CleOrganisation.Statut statut);

    Optional<CleOrganisation> findByTenantIdAndVersion(Long tenantId, int version);

    List<CleOrganisation> findByTenantIdOrderByVersionDesc(Long tenantId);
}
