package qc.levis.contrat.formulaire.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.formulaire.domain.Formulaire;

public interface FormulaireRepository extends JpaRepository<Formulaire, Long> {
    List<Formulaire> findByTenantIdOrderByNomAsc(Long tenantId);
    List<Formulaire> findByTenantIdAndDomaineOrderByNomAsc(Long tenantId, String domaine);
    Optional<Formulaire> findByIdAndTenantId(Long id, Long tenantId);
    Optional<Formulaire> findByTenantIdAndCode(Long tenantId, String code);
    boolean existsByTenantIdAndCode(Long tenantId, String code);
}
