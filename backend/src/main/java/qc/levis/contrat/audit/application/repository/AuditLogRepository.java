package qc.levis.contrat.audit.application.repository;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import qc.levis.contrat.audit.domain.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * Recherche filtrée du journal, scopée au tenant (art. 18). Chaque filtre est optionnel :
     * un paramètre nul désactive le critère correspondant.
     */
    @Query("""
            SELECT a FROM AuditLog a
            WHERE (:tenantId IS NULL OR a.tenantId = :tenantId)
              AND (:utilisateur IS NULL OR a.utilisateur = :utilisateur)
              AND (:action IS NULL OR a.action = :action)
              AND (:entite IS NULL OR a.entite = :entite)
              AND (CAST(:depuis AS timestamp) IS NULL OR a.horodatage >= :depuis)
              AND (CAST(:jusqua AS timestamp) IS NULL OR a.horodatage <= :jusqua)
            ORDER BY a.horodatage DESC
            """)
    List<AuditLog> rechercher(@Param("tenantId") Long tenantId,
                              @Param("utilisateur") String utilisateur,
                              @Param("action") String action,
                              @Param("entite") String entite,
                              @Param("depuis") Instant depuis,
                              @Param("jusqua") Instant jusqua);
}
