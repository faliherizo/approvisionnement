package qc.levis.contrat.formulaire.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.formulaire.domain.FormulaireVersion;

public interface FormulaireVersionRepository extends JpaRepository<FormulaireVersion, Long> {
    List<FormulaireVersion> findByFormulaireIdOrderByNumeroDesc(Long formulaireId);
    Optional<FormulaireVersion> findByFormulaireIdAndNumero(Long formulaireId, int numero);
    Optional<FormulaireVersion> findTopByFormulaireIdOrderByNumeroDesc(Long formulaireId);
}
