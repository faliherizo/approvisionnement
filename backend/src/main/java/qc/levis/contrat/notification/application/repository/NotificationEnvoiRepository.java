package qc.levis.contrat.notification.application.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.notification.domain.NotificationEnvoi;

public interface NotificationEnvoiRepository extends JpaRepository<NotificationEnvoi, Long> {
    List<NotificationEnvoi> findTop100ByTenantIdOrderByEnvoyeLeDesc(Long tenantId);
    List<NotificationEnvoi> findByTenantIdAndDestinataireIgnoreCase(Long tenantId, String destinataire);
}
