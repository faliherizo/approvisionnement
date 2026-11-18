package qc.levis.contrat.clm.application.repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import qc.levis.contrat.clm.domain.CompteurReference;

public interface CompteurReferenceRepository extends JpaRepository<CompteurReference, Long> {

    /** Verrou exclusif jusqu'à la fin de la transaction de création. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<CompteurReference> findByTenantIdAndPrefixeAndAnnee(Long tenantId, String prefixe, int annee);
}
