package qc.levis.contrat.redaction.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.redaction.domain.Gabarit;

public interface GabaritRepository extends JpaRepository<Gabarit, Long> {
    List<Gabarit> findByTenantIdIsNullOrderByOrdreAscNomAsc();
    List<Gabarit> findByTenantIdOrderByOrdreAscNomAsc(Long tenantId);
    Optional<Gabarit> findByTenantIdAndCode(Long tenantId, String code);
    Optional<Gabarit> findByTenantIdIsNullAndCode(String code);
    long countByTenantIdIsNull();
}
