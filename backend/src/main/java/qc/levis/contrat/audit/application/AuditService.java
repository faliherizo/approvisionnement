package qc.levis.contrat.audit.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import qc.levis.contrat.audit.application.repository.AuditLogRepository;
import qc.levis.contrat.audit.domain.AuditLog;
import qc.levis.contrat.commun.application.LogSafe;
import qc.levis.contrat.commun.infrastructure.TenantContext;

/** Journalisation d'audit (traçabilité - Loi 25). */
@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository repo;

    public AuditService(AuditLogRepository repo) {
        this.repo = repo;
    }

    public void log(String utilisateur, String action, String entite, Long id, String details) {
        journaliser(TenantContext.get(), utilisateur, action, entite, id, details);
    }

    /**
     * Journalise avec un tenant explicite. Utile pour les événements d'authentification, écrits
     * avant que le {@link TenantContext} ne soit alimenté (connexion) : on rattache alors la trace
     * au tenant du compte concerné afin qu'elle reste consultable par son administrateur (exig. 9).
     */
    public void log(Long tenantId, String utilisateur, String action, String entite, Long id, String details) {
        journaliser(tenantId, utilisateur, action, entite, id, details);
    }

    private void journaliser(Long tenantId, String utilisateur, String action, String entite, Long id, String details) {
        AuditLog entree = new AuditLog(utilisateur, action, entite, id, details);
        entree.setTenantId(tenantId);
        repo.save(entree);
        // Trace aussi l'action utilisateur dans les journaux techniques (courriel masqué — Loi 25).
        // L'identité complète reste dans le journal d'audit en base.
        log.info("Action utilisateur : {} · {} {}#{} (tenant {}){}",
                LogSafe.courriel(utilisateur), action, entite, id, tenantId,
                details != null && !details.isBlank() ? " · " + details : "");
    }
}
