package qc.levis.contrat.audit.application.repository;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import qc.levis.contrat.audit.domain.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    long countByUtilisateurIgnoreCaseAndActionAndHorodatageAfter(String utilisateur, String action, Instant depuis);

    /** Lecture par curseur (identifiant croissant) du journal d'une organisation, pour un SIEM (Annexe 10, TI-04). */
    List<AuditLog> findByTenantIdAndIdGreaterThanOrderByIdAsc(Long tenantId, Long apres, org.springframework.data.domain.Pageable page);

    /** Entrées dont la personne est l'auteur ou que son courriel mentionne. */
    @Query("""
            select a from AuditLog a where a.tenantId = :tenantId
              and (lower(a.utilisateur) = lower(:courriel) or lower(a.details) like lower(concat('%', :courriel, '%')))
            order by a.horodatage desc
            """)
    List<AuditLog> deLaPersonne(@Param("tenantId") Long tenantId, @Param("courriel") String courriel);


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
