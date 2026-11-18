package qc.levis.contrat.clm.application.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.clm.domain.EvenementTrousse;

public interface EvenementTrousseRepository extends JpaRepository<EvenementTrousse, Long> {
    List<EvenementTrousse> findByTenantIdAndTrousseIdOrderBySequenceAsc(Long tenantId, Long trousseId);

    Optional<EvenementTrousse> findTopByTenantIdAndTrousseIdOrderBySequenceDesc(Long tenantId, Long trousseId);

    List<EvenementTrousse> findTop30ByTenantIdOrderByHorodatageDesc(Long tenantId);

    List<EvenementTrousse> findByTenantIdAndHorodatageBetweenOrderByHorodatageDesc(Long tenantId, Instant du, Instant au);

    /** Dernier événement de chacune des trousses d'une page du suivi, en une requête. */
    @org.springframework.data.jpa.repository.Query("select e from EvenementTrousse e where e.tenantId = :tenantId and e.trousseId in :ids "
            + "and e.sequence = (select max(d.sequence) from EvenementTrousse d where d.tenantId = e.tenantId and d.trousseId = e.trousseId)")
    List<EvenementTrousse> derniersParTenantIdEtTrousses(@org.springframework.data.repository.query.Param("tenantId") Long tenantId,
                                                       @org.springframework.data.repository.query.Param("ids") java.util.Collection<Long> ids);

    /** Trousses ayant reçu un événement de ce type avant la date : une seule requête, quelle que soit la volumétrie. */
    @org.springframework.data.jpa.repository.Query("select distinct e.trousseId from EvenementTrousse e "
            + "where e.tenantId = :tenantId and e.type = :type and e.horodatage < :avant")
    java.util.Set<Long> trousseIdsParTenantIdTypeAvant(@org.springframework.data.repository.query.Param("tenantId") Long tenantId,
                                               @org.springframework.data.repository.query.Param("type") String type,
                                               @org.springframework.data.repository.query.Param("avant") Instant avant);
}
