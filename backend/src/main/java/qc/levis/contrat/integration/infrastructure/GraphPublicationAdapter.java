package qc.levis.contrat.integration.infrastructure;

import java.util.EnumSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import qc.levis.contrat.commun.domain.Enums;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.integration.application.IntegrationM365Service;
import qc.levis.contrat.integration.application.port.Microsoft365Client;
import qc.levis.contrat.integration.application.port.PublicationCiblePort;

/**
 * Adaptateur de publication Microsoft 365 réel (Graph), prioritaire sur la simulation. Il ne prend
 * en charge une cible M365 que lorsque le tenant courant possède une configuration <em>active et
 * complète</em> (saisie dans l'espace admin) : sinon {@link #supporte} renvoie {@code false} et la
 * publication retombe sur l'adaptateur de simulation (OCP/DIP, ADR-002). Aucune bascule globale : la
 * décision est prise par tenant.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GraphPublicationAdapter implements PublicationCiblePort {

    private static final Logger log = LoggerFactory.getLogger(GraphPublicationAdapter.class);

    private static final Set<Enums.CiblePublication> CIBLES_M365 = EnumSet.of(
            Enums.CiblePublication.SHAREPOINT, Enums.CiblePublication.ONEDRIVE,
            Enums.CiblePublication.OUTLOOK, Enums.CiblePublication.TEAMS);

    private final IntegrationM365Service config;
    private final Microsoft365Client client;

    public GraphPublicationAdapter(IntegrationM365Service config, Microsoft365Client client) {
        this.config = config;
        this.client = client;
    }

    @Override
    public boolean supporte(Enums.CiblePublication cible) {
        return CIBLES_M365.contains(cible) && config.actifEtConfigure(TenantContext.get());
    }

    @Override
    public ResultatPublication deposer(Enums.CiblePublication cible, ContenuPublie contenu) {
        Microsoft365Client.ConfigM365 cfg = config.configClient(TenantContext.get()).orElse(null);
        if (cfg == null) {
            log.warn("Dépôt Microsoft 365 impossible pour le tenant {} : configuration absente ou inactive",
                    TenantContext.get());
            return new ResultatPublication(false, null, "Configuration Microsoft 365 absente ou inactive.");
        }
        try {
            log.debug("Dépôt Microsoft Graph vers {} (document {}, dossier '{}')",
                    cible, contenu.documentId(), contenu.destination());
            return client.deposer(cfg, cible, contenu);
        } catch (RuntimeException e) {
            // On journalise le message d'erreur, jamais la configuration ni le secret.
            log.error("Échec du dépôt Microsoft 365 vers {} (document {}) : {}",
                    cible, contenu.documentId(), e.getMessage());
            return new ResultatPublication(false, null, "Échec Microsoft 365 : " + e.getMessage());
        }
    }
}
