package qc.levis.contrat.commun.module.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.commun.module.domain.ModuleCatalogue;

public interface ModuleCatalogueRepository extends JpaRepository<ModuleCatalogue, Long> {
    List<ModuleCatalogue> findAllByOrderByOrdreAsc();
    Optional<ModuleCatalogue> findByCode(String code);
    boolean existsByCode(String code);
}
