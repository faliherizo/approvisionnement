package qc.levis.contrat.clm.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.clm.domain.ModeleMessage;

public interface ModeleMessageRepository extends JpaRepository<ModeleMessage, Long> {
    Optional<ModeleMessage> findByIdAndTenantId(Long id, Long tenantId);

    List<ModeleMessage> findByTenantIdOrderByEvenementAscCanalAsc(Long tenantId);

    List<ModeleMessage> findByTenantIdAndEvenement(Long tenantId, ModeleMessage.Evenement evenement);
}
