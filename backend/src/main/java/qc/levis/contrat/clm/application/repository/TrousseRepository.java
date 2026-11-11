package qc.levis.contrat.clm.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.clm.domain.StatutTrousse;
import qc.levis.contrat.clm.domain.Trousse;

public interface TrousseRepository extends JpaRepository<Trousse, Long> {
    Optional<Trousse> findByIdAndTenantId(Long id, Long tenantId);

    List<Trousse> findByTenantIdOrderByMajLeDesc(Long tenantId);

    /** Trousses récentes d'un initiateur : source des suggestions de saisie, jamais celles d'autrui. */
    List<Trousse> findTop200ByTenantIdAndProprietaireIgnoreCaseOrderByMajLeDesc(Long tenantId, String proprietaire);

    boolean existsByTenantIdAndReference(Long tenantId, String reference);

    long countByTenantIdAndStatut(Long tenantId, StatutTrousse statut);

    long countByTenantIdAndModeleId(Long tenantId, Long modeleId);
}
