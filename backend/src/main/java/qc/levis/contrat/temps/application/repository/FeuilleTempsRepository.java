package qc.levis.contrat.temps.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.temps.domain.FeuilleTemps;

public interface FeuilleTempsRepository extends JpaRepository<FeuilleTemps, Long> {
    List<FeuilleTemps> findByTenantIdAndProprietaireOrderByMajLeDesc(Long tenantId, String proprietaire);
    List<FeuilleTemps> findByTenantIdAndStatutOrderByMajLeDesc(Long tenantId, String statut);
    Optional<FeuilleTemps> findByIdAndTenantId(Long id, Long tenantId);
}
