package qc.levis.contrat.clm.infrastructure;

import java.util.List;
import org.springframework.stereotype.Component;
import qc.levis.contrat.audit.application.AuditQueryService;
import qc.levis.contrat.clm.application.port.JournalSecurite;
import qc.levis.contrat.clm.domain.EvenementSiem;

/** Le journal de sécurité est le journal d'audit de la plateforme, lu par son service et non par ses tables (ADR-006). */
@Component
public class JournalSecuriteAudit implements JournalSecurite {

    private final AuditQueryService audit;

    public JournalSecuriteAudit(AuditQueryService audit) {
        this.audit = audit;
    }

    @Override
    public List<EvenementSiem> depuis(Long tenantId, long curseur, int limite) {
        return audit.depuis(tenantId, curseur, limite).stream()
                .map(e -> new EvenementSiem(e.id(), e.horodatage(), e.utilisateur(), e.action(), e.entite(), e.entiteId(), e.details()))
                .toList();
    }
}
