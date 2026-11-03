package qc.levis.contrat.clm.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.clm.domain.Invitation;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    /** Recherche hors tenant : le portail public ne connaît que le jeton, le tenant en découle. */
    Optional<Invitation> findByJetonHash(String jetonHash);

    List<Invitation> findByTenantIdAndTrousseIdOrderByIdAsc(Long tenantId, Long trousseId);

    List<Invitation> findByTenantIdAndEtapeId(Long tenantId, Long etapeId);
}
