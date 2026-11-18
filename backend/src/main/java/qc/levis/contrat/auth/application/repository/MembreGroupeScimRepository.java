package qc.levis.contrat.auth.application.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.auth.domain.MembreGroupeScim;

public interface MembreGroupeScimRepository extends JpaRepository<MembreGroupeScim, Long> {
    List<MembreGroupeScim> findByTenantIdAndGroupeId(Long tenantId, String groupeId);

    List<MembreGroupeScim> findByTenantIdAndUtilisateurId(Long tenantId, Long utilisateurId);

    boolean existsByTenantIdAndGroupeIdAndUtilisateurId(Long tenantId, String groupeId, Long utilisateurId);

    long deleteByTenantIdAndGroupeIdAndUtilisateurId(Long tenantId, String groupeId, Long utilisateurId);

    long deleteByTenantIdAndGroupeId(Long tenantId, String groupeId);
}
