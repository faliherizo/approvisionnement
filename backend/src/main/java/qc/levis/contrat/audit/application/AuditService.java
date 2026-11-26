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
    /** Nombre d'événements d'une action pour une personne depuis un instant (ex. échecs de connexion récents). */
    public long compter(String utilisateur, String action, java.time.Instant depuis) {
        return repo.countByUtilisateurIgnoreCaseAndActionAndHorodatageAfter(utilisateur, action, depuis);
    }

    public void log(Long tenantId, String utilisateur, String action, String entite, Long id, String details) {
        journaliser(tenantId, utilisateur, action, entite, id, details);
    }

    /**
     * Action dont le détail porte des données d'affaires (valeurs avant/après d'un champ) : le détail est la preuve et reste
     * dans le journal d'audit en base, lisible par qui en a le droit ; le journal technique n'en garde que l'existence
     * (Loi 25, docs/journalisation.md).
     */
    public void logConfidentiel(Long tenantId, String utilisateur, String action, String entite, Long id, String details) {
        AuditLog entree = new AuditLog(utilisateur, action, entite, id, details);
        entree.setTenantId(tenantId);
        repo.save(entree);
        log.info("Action utilisateur : {} · {} {}#{} (tenant {}) · détail au journal d'audit",
                LogSafe.courriel(utilisateur), action, entite, id, tenantId);
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
