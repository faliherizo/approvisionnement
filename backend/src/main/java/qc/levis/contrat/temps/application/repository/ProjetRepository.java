package qc.levis.contrat.temps.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.temps.domain.Projet;

public interface ProjetRepository extends JpaRepository<Projet, Long> {
    List<Projet> findByTenantIdOrderByNomAsc(Long tenantId);
    Optional<Projet> findByIdAndTenantId(Long id, Long tenantId);
    boolean existsByTenantIdAndCode(Long tenantId, String code);
}
