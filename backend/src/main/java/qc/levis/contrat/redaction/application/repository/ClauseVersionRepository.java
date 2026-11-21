package qc.levis.contrat.redaction.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.redaction.domain.ClauseVersion;

public interface ClauseVersionRepository extends JpaRepository<ClauseVersion, Long> {
    List<ClauseVersion> findByClauseIdOrderByNumeroDesc(Long clauseId);
    Optional<ClauseVersion> findFirstByClauseIdAndNumeroOrderByIdDesc(Long clauseId, int numero);
    List<ClauseVersion> findByTenantIdAndAuteurIgnoreCase(Long tenantId, String auteur);
}
