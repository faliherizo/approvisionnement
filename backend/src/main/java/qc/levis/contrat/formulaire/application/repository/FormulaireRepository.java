package qc.levis.contrat.formulaire.application.repository;

import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import qc.levis.contrat.formulaire.domain.Formulaire;

public interface FormulaireRepository extends JpaRepository<Formulaire, Long> {
    List<Formulaire> findByTenantIdOrderByNomAsc(Long tenantId);
    List<Formulaire> findByTenantIdAndDomaineOrderByNomAsc(Long tenantId, String domaine);
    Optional<Formulaire> findByIdAndTenantId(Long id, Long tenantId);
    Optional<Formulaire> findByTenantIdAndCode(Long tenantId, String code);
    boolean existsByTenantIdAndCode(Long tenantId, String code);

    /**
     * Verrouille le formulaire jusqu'à la fin de la transaction : les soumissions d'un même formulaire se suivent, et deux
     * personnes ne peuvent pas prendre ensemble la dernière place d'un choix ou le dernier rang de répondant.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select f from Formulaire f where f.id = :id and f.tenantId = :tenantId")
    Optional<Formulaire> verrouiller(@Param("id") Long id, @Param("tenantId") Long tenantId);
}
