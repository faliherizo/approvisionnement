package qc.levis.contrat.notification.application.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.notification.domain.NotificationRegle;

public interface NotificationRegleRepository extends JpaRepository<NotificationRegle, Long> {
    List<NotificationRegle> findByTenantId(Long tenantId);
    List<NotificationRegle> findByTenantIdAndFormulaireIdAndDeclencheurAndActifTrue(Long tenantId, Long formulaireId, String declencheur);
}
