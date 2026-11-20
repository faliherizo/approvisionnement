package qc.levis.contrat.clm.application.repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.clm.domain.MessageSortant;

public interface MessageSortantRepository extends JpaRepository<MessageSortant, Long> {
    Optional<MessageSortant> findByIdAndTenantId(Long id, Long tenantId);

    boolean existsByCleIdempotence(String cleIdempotence);

    List<MessageSortant> findTop100ByTenantIdOrderByCreeLeDesc(Long tenantId);

    List<MessageSortant> findTop50ByTenantIdAndStatutOrderByCreeLeDesc(Long tenantId, MessageSortant.Statut statut);

    List<MessageSortant> findByTenantIdAndTrousseIdInAndStatut(Long tenantId, Collection<Long> trousseIds, MessageSortant.Statut statut);

    /** Relais tous tenants confondus : réservé au planificateur. */
    List<MessageSortant> findTop50ByStatutAndProchainEssaiLessThanEqualOrderByProchainEssaiAsc(MessageSortant.Statut statut, Instant maintenant);
}
