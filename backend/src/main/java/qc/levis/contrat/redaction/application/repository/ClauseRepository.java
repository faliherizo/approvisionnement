package qc.levis.contrat.redaction.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import qc.levis.contrat.redaction.domain.Clause;

public interface ClauseRepository extends JpaRepository<Clause, Long> {
    List<Clause> findByActifTrue();
    Optional<Clause> findByCode(String code);

    // --- Scopé par tenant ---
    List<Clause> findByActifTrueAndTenantId(Long tenantId);
    Optional<Clause> findByCodeAndTenantId(String code, Long tenantId);

    /** Recherche paginée (code, titre, corps) sur le périmètre du tenant courant. */
    @Query("""
            select c from Clause c
            where c.actif = true
              and ((:tid is null and c.tenantId is null) or c.tenantId = :tid)
              and (:q = '' or lower(c.code)  like lower(concat('%', :q, '%'))
                           or lower(c.titre) like lower(concat('%', :q, '%'))
                           or lower(c.corps) like lower(concat('%', :q, '%')))
            """)
    Page<Clause> rechercher(@Param("tid") Long tid, @Param("q") String q, Pageable pageable);
}
