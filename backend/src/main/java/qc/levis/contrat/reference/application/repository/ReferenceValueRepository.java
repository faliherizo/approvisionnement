package qc.levis.contrat.reference.application.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.reference.domain.ReferenceValue;

public interface ReferenceValueRepository extends JpaRepository<ReferenceValue, Long> {
    List<ReferenceValue> findByCategorieAndTenantIdIsNullOrderByOrdreAsc(String categorie);
    List<ReferenceValue> findByCategorieAndTenantIdOrderByOrdreAsc(String categorie, Long tenantId);
    boolean existsByCategorieAndTenantIdIsNull(String categorie);
    boolean existsByCategorieAndCodeAndTenantIdIsNull(String categorie, String code);
    boolean existsByCategorieAndCodeAndTenantId(String categorie, String code, Long tenantId);
}
