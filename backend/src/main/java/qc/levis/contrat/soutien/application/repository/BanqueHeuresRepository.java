package qc.levis.contrat.soutien.application.repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import qc.levis.contrat.soutien.domain.BanqueHeures;

public interface BanqueHeuresRepository extends JpaRepository<BanqueHeures, Long> {
    List<BanqueHeures> findByTenantIdOrderByDebutDesc(Long tenantId);
    Optional<BanqueHeures> findByIdAndTenantId(Long id, Long tenantId);

    /** Verrou le temps de la transaction : deux consommations simultanées ne peuvent pas dépasser le solde. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from BanqueHeures b where b.id = :id and b.tenantId = :tenantId")
    Optional<BanqueHeures> verrouiller(@Param("id") Long id, @Param("tenantId") Long tenantId);
}
