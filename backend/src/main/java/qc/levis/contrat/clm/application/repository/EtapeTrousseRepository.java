package qc.levis.contrat.clm.application.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.clm.domain.EtapeTrousse;
import qc.levis.contrat.clm.domain.Parcours;

public interface EtapeTrousseRepository extends JpaRepository<EtapeTrousse, Long> {
    Optional<EtapeTrousse> findByIdAndTenantId(Long id, Long tenantId);

    List<EtapeTrousse> findByTenantIdAndTrousseIdOrderByPhaseAscIdAsc(Long tenantId, Long trousseId);

    List<EtapeTrousse> findByTenantIdAndStatut(Long tenantId, Parcours.StatutEtape statut);

    List<EtapeTrousse> findByTenantIdAndTrousseIdInAndStatut(Long tenantId, Collection<Long> trousseIds, Parcours.StatutEtape statut);

    /** Toutes les étapes actives, tous tenants confondus : réservé au planificateur des rappels. */
    List<EtapeTrousse> findByStatut(Parcours.StatutEtape statut);
}
