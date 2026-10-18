package qc.levis.contrat.ia.application;

import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.audit.application.AuditService;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.ia.application.repository.AiSuggestionRepository;
import qc.levis.contrat.ia.domain.AiSuggestion;
import qc.levis.contrat.tenant.application.repository.TenantRepository;
import qc.levis.contrat.tenant.domain.Tenant;

/**
 * Encadrement de l'assistant IA (art. 21) : option de désactivation par tenant et traçabilité des
 * suggestions (invite, réponse, modèle, utilisateur). L'IA reste locale (Ollama) — aucune donnée
 * transmise à l'externe.
 */
@Service
public class AiTraceabiliteService {

    private static final int MAX_INVITE = 4000;
    private static final int MAX_REPONSE = 8000;

    private final TenantRepository tenants;
    private final AiSuggestionRepository suggestions;
    private final AuditService audit;

    public AiTraceabiliteService(TenantRepository tenants, AiSuggestionRepository suggestions,
                                 AuditService audit) {
        this.tenants = tenants;
        this.suggestions = suggestions;
        this.audit = audit;
    }

    /** L'IA est-elle activée pour le tenant courant ? (superadmin/global : toujours oui). */
    public boolean estActivee() {
        Long tenant = TenantContext.get();
        if (tenant == null) {
            return true;
        }
        return tenants.findById(tenant).map(Tenant::isIaActivee).orElse(true);
    }

    /** Refuse l'usage de l'IA si elle est désactivée pour le tenant (art. 21). */
    public void verifierActivee() {
        if (!estActivee()) {
            throw new IllegalStateException("Assistant IA désactivé pour ce tenant (art. 21).");
        }
    }

    /** Active ou désactive l'IA pour le tenant courant. */
    @Transactional
    public boolean activer(boolean activee, String utilisateur) {
        Long tenant = TenantContext.get();
        if (tenant == null) {
            throw new IllegalStateException("Aucun tenant courant : activation IA impossible.");
        }
        Tenant t = tenants.findById(tenant)
                .orElseThrow(() -> new NoSuchElementException("Tenant introuvable: " + tenant));
        t.setIaActivee(activee);
        tenants.save(t);
        audit.log(utilisateur, activee ? "IA_ACTIVEE" : "IA_DESACTIVEE", "Tenant", tenant, null);
        return activee;
    }

    /** Journalise une suggestion IA (traçabilité). */
    @Transactional
    public AiSuggestion journaliser(String type, String invite, String reponse, String modele,
                                    String utilisateur) {
        return suggestions.save(new AiSuggestion(type, tronquer(invite, MAX_INVITE),
                tronquer(reponse, MAX_REPONSE), modele, utilisateur, TenantContext.get()));
    }

    /** Suggestions journalisées du tenant courant, plus récentes d'abord. */
    public List<AiSuggestion> lister() {
        return suggestions.findByTenantIdOrderByCreeLeDesc(TenantContext.get());
    }

    private static String tronquer(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() <= max ? s : s.substring(0, max);
    }
}
