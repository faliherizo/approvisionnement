package qc.levis.contrat.clm.application.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import qc.levis.contrat.clm.domain.GabaritWord;

public interface GabaritWordRepository extends JpaRepository<GabaritWord, Long> {

    Optional<GabaritWord> findByModeleIdAndTenantId(Long modeleId, Long tenantId);

    /** Nom seul, sans charger le document : la fiche du modèle l'affiche. */
    @Query("select g.nom from GabaritWord g where g.modeleId = :modeleId and g.tenantId = :tenantId")
    Optional<String> nomParModeleIdEtTenantId(@Param("modeleId") Long modeleId, @Param("tenantId") Long tenantId);

    void deleteByModeleIdAndTenantId(Long modeleId, Long tenantId);
}
