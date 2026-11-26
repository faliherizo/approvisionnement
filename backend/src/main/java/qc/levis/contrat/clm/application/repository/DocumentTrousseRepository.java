package qc.levis.contrat.clm.application.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import qc.levis.contrat.clm.domain.DocumentSigne;
import qc.levis.contrat.clm.domain.DocumentTrousse;

public interface DocumentTrousseRepository extends JpaRepository<DocumentTrousse, Long> {
    Optional<DocumentTrousse> findByIdAndTenantId(Long id, Long tenantId);

    List<DocumentTrousse> findByTenantIdAndTrousseIdOrderByCleDocumentAscVersionDesc(Long tenantId, Long trousseId);

    List<DocumentTrousse> findByTenantIdAndTrousseIdAndCleDocumentOrderByVersionDesc(Long tenantId, Long trousseId, String cleDocument);

    long countByTenantIdAndCleVersion(Long tenantId, Integer cleVersion);

    long countByTenantIdAndCleVersionIsNull(Long tenantId);

    /** Documents à réenvelopper (ADR-037) : anciens contenus en clair et versions de clé autres que l'active. */
    @Query("select d.id from DocumentTrousse d where d.tenantId = :tenantId and (d.cleVersion is null or d.cleVersion <> :version)"
            + " order by d.id")
    List<Long> findIdsByTenantIdHorsVersion(@Param("tenantId") Long tenantId, @Param("version") int version);

    /**
     * Versions signées des trousses terminées dans la période (AF-49), avec l'unité de la trousse : une seule requête, sans
     * charger les contenus, quel que soit le nombre de trousses.
     */
    @Query("select new qc.levis.contrat.clm.domain.DocumentSigne(d.id, t.unite, e.horodatage, d.pages)"
            + " from DocumentTrousse d, Trousse t, EvenementTrousse e"
            + " where d.tenantId = :tenantId and t.tenantId = :tenantId and e.tenantId = :tenantId"
            + " and t.id = d.trousseId and e.trousseId = d.trousseId and e.type = 'TROUSSE_TERMINEE'"
            + " and e.horodatage between :debut and :fin and d.statut = qc.levis.contrat.clm.domain.DocumentTrousse.Statut.SIGNE"
            + " order by e.horodatage")
    List<DocumentSigne> signesDansPeriode(@Param("tenantId") Long tenantId, @Param("debut") Instant debut, @Param("fin") Instant fin);
}
