package qc.levis.contrat.auth.application.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.auth.domain.AppartenanceAnnuaire;

public interface AppartenanceAnnuaireRepository extends JpaRepository<AppartenanceAnnuaire, Long> {
    List<AppartenanceAnnuaire> findByTenantIdAndCourriel(Long tenantId, String courriel);

    long deleteByTenantIdAndCourrielAndOrigine(Long tenantId, String courriel, AppartenanceAnnuaire.Origine origine);
}
