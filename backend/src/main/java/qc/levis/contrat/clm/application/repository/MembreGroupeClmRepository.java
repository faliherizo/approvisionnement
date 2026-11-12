package qc.levis.contrat.clm.application.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.clm.domain.MembreGroupeClm;

public interface MembreGroupeClmRepository extends JpaRepository<MembreGroupeClm, Long> {
    List<MembreGroupeClm> findByTenantIdAndCourrielIgnoreCase(Long tenantId, String courriel);

    List<MembreGroupeClm> findByTenantIdAndGroupeIdOrderByCourrielAsc(Long tenantId, Long groupeId);

    boolean existsByTenantIdAndGroupeIdAndCourrielIgnoreCase(Long tenantId, Long groupeId, String courriel);

    long deleteByTenantIdAndGroupeIdAndCourrielIgnoreCase(Long tenantId, Long groupeId, String courriel);

    long deleteByTenantIdAndGroupeId(Long tenantId, Long groupeId);
}
