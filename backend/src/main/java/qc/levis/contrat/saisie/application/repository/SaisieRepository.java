package qc.levis.contrat.saisie.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.saisie.domain.Saisie;
import qc.levis.contrat.saisie.domain.StatutSaisie;

public interface SaisieRepository extends JpaRepository<Saisie, Long> {
    List<Saisie> findByTenantIdAndFormulaireIdOrderByMajLeDesc(Long tenantId, Long formulaireId);
    List<Saisie> findByTenantIdAndCreeParOrderByMajLeDesc(Long tenantId, String creePar);
    Optional<Saisie> findByIdAndTenantId(Long id, Long tenantId);
    long countByFormulaireIdAndStatutNot(Long formulaireId, StatutSaisie statut);
}
