package qc.levis.contrat.reference.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.reference.domain.CategorieReference;

public interface CategorieReferenceRepository extends JpaRepository<CategorieReference, Long> {
    Optional<CategorieReference> findByCode(String code);
    List<CategorieReference> findAllByOrderByOrdreAscCodeAsc();
}
