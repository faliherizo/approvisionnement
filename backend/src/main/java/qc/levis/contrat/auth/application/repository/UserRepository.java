package qc.levis.contrat.auth.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import qc.levis.contrat.auth.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    List<User> findByTenantIdAndActifTrueOrderByNomAsc(Long tenantId);

    List<User> findByTenantIdOrderByEmailAsc(Long tenantId);

    /**
     * Recherche paginée des utilisateurs d'un tenant (le superadmin, dont tenantId
     * est null, est automatiquement exclu). Recherche avancée sur courriel et nom.
     */
    @Query("""
            select u from User u
            where u.tenantId = :tid
              and (:q = '' or lower(u.email) like lower(concat('%', :q, '%'))
                          or lower(u.nom)   like lower(concat('%', :q, '%')))
            """)
    Page<User> rechercher(@Param("tid") Long tid, @Param("q") String q, Pageable pageable);
}
