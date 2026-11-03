package qc.levis.contrat.notification.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.notification.domain.NotificationModele;

public interface NotificationModeleRepository extends JpaRepository<NotificationModele, Long> {
    List<NotificationModele> findByTenantId(Long tenantId);
    Optional<NotificationModele> findByTenantIdAndCode(Long tenantId, String code);
}
