package qc.levis.contrat.clm.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.clm.domain.StatutTrousse;
import qc.levis.contrat.clm.domain.Trousse;

public interface TrousseRepository extends JpaRepository<Trousse, Long>,
        org.springframework.data.jpa.repository.JpaSpecificationExecutor<Trousse> {
    Optional<Trousse> findByIdAndTenantId(Long id, Long tenantId);

    List<Trousse> findByTenantIdOrderByMajLeDesc(Long tenantId);

    long countByTenantIdAndReferenceStartingWith(Long tenantId, String prefixe);

    List<Trousse> findByTenantIdAndIdIn(Long tenantId, java.util.Collection<Long> ids);


    /** Trousses récentes d'un initiateur : source des suggestions de saisie, jamais celles d'autrui. */
    List<Trousse> findTop200ByTenantIdAndProprietaireIgnoreCaseOrderByMajLeDesc(Long tenantId, String proprietaire);

    /** Trousses ouvertes d'une personne : celles dont la propriété suit un départ (P3). */
    List<Trousse> findByTenantIdAndProprietaireIgnoreCaseAndStatutInOrderByIdAsc(Long tenantId, String proprietaire,
                                                                               java.util.Collection<StatutTrousse> statuts);

    /** Trousses closes encore conservées : candidates au calendrier de conservation. */
    List<Trousse> findByTenantIdAndStatutInAndDetruiteLeIsNullOrderByClotureLeAsc(Long tenantId, java.util.Collection<StatutTrousse> statuts);

    List<Trousse> findByTenantIdAndDetruiteLeIsNotNullOrderByDetruiteLeDesc(Long tenantId);

    boolean existsByTenantIdAndReference(Long tenantId, String reference);

    Optional<Trousse> findByTenantIdAndReference(Long tenantId, String reference);

    long countByTenantIdAndStatut(Long tenantId, StatutTrousse statut);

    long countByTenantIdAndModeleId(Long tenantId, Long modeleId);
}
