package qc.levis.contrat.auth.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.auth.domain.CorrespondanceAnnuaire;

public interface CorrespondanceAnnuaireRepository extends JpaRepository<CorrespondanceAnnuaire, Long> {
    List<CorrespondanceAnnuaire> findByTenantIdOrderByIdAsc(Long tenantId);

    Optional<CorrespondanceAnnuaire> findByIdAndTenantId(Long id, Long tenantId);
}
