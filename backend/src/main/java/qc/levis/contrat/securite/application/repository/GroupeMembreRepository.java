package qc.levis.contrat.securite.application.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.securite.domain.GroupeMembre;

public interface GroupeMembreRepository extends JpaRepository<GroupeMembre, Long> {
    List<GroupeMembre> findByTenantIdAndUtilisateur(Long tenantId, String utilisateur);
}
