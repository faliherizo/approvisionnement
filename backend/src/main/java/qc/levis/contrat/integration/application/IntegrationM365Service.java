package qc.levis.contrat.integration.application;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.commun.application.SecretCipher;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.integration.application.dto.IntegrationM365Request;
import qc.levis.contrat.integration.application.dto.IntegrationM365Vue;
import qc.levis.contrat.integration.application.port.Microsoft365Client;
import qc.levis.contrat.integration.application.repository.IntegrationM365Repository;
import qc.levis.contrat.integration.domain.IntegrationM365;

/**
 * Configuration Microsoft 365 par tenant (SharePoint/OneDrive/Teams/Outlook), gérée depuis l'espace
 * admin. Chiffre le secret client au repos et ne l'expose jamais en clair. Détermine si un tenant
 * bascule en mode réel (config active et complète) ou reste en simulation.
 */
@Service
public class IntegrationM365Service {

    private static final Logger log = LoggerFactory.getLogger(IntegrationM365Service.class);

    private final IntegrationM365Repository repo;
    private final SecretCipher cipher;

    public IntegrationM365Service(IntegrationM365Repository repo, SecretCipher cipher) {
        this.repo = repo;
        this.cipher = cipher;
    }

    /** Configuration du tenant courant, ou une configuration vierge (inactive) si absente. */
    public IntegrationM365 courant() {
        return pour(TenantContext.get());
    }

    public IntegrationM365 pour(Long tenantId) {
        return repo.findByTenantId(tenantId).orElseGet(() -> {
            IntegrationM365 vide = new IntegrationM365();
            vide.setTenantId(tenantId);
            return vide;
        });
    }

    /** Vue destinée au frontend : jamais le secret, seulement l'indication de sa présence. */
    public IntegrationM365Vue vue(Long tenantId) {
        return vueDe(pour(tenantId));
    }

    private IntegrationM365Vue vueDe(IntegrationM365 c) {
        return new IntegrationM365Vue(c.isActif(), c.getMsTenantId(), c.getClientId(),
                c.getSharepointSiteId(), c.getDriveId(), c.getTeamsTeamId(), c.getTeamsChannelId(),
                c.getOutlookExpediteur(), c.getDossierRacine(),
                c.getClientSecretChiffre() != null && !c.getClientSecretChiffre().isBlank());
    }

    @Transactional
    public IntegrationM365Vue enregistrer(Long tenantId, IntegrationM365Request req) {
        IntegrationM365 c = repo.findByTenantId(tenantId).orElseGet(IntegrationM365::new);
        c.setTenantId(tenantId);
        c.setActif(req.actif() != null && req.actif());
        c.setMsTenantId(req.msTenantId());
        c.setClientId(req.clientId());
        c.setSharepointSiteId(req.sharepointSiteId());
        c.setDriveId(req.driveId());
        c.setTeamsTeamId(req.teamsTeamId());
        c.setTeamsChannelId(req.teamsChannelId());
        c.setOutlookExpediteur(req.outlookExpediteur());
        c.setDossierRacine(req.dossierRacine());
        // Secret write-only : ne l'écrase que si un nouveau est fourni ; sinon conserve l'existant.
        if (req.clientSecret() != null && !req.clientSecret().isBlank()) {
            c.setClientSecretChiffre(cipher.chiffrer(req.clientSecret()));
        }
        IntegrationM365 sauve = repo.save(c);
        // On journalise le tenant et l'état, JAMAIS le secret ni les identifiants Graph.
        log.info("Configuration Microsoft 365 enregistrée pour le tenant {} (actif={}, secret défini={})",
                tenantId, sauve.isActif(), sauve.getClientSecretChiffre() != null);
        return vueDe(sauve);
    }

    /** Vrai si le tenant a une configuration active et complète (→ dépôts réels via Graph). */
    public boolean actifEtConfigure(Long tenantId) {
        IntegrationM365 c = pour(tenantId);
        return c.isActif() && c.estConfigure();
    }

    /** Configuration déchiffrée pour le client Graph, si active et complète. */
    public Optional<Microsoft365Client.ConfigM365> configClient(Long tenantId) {
        IntegrationM365 c = pour(tenantId);
        if (!c.isActif() || !c.estConfigure()) return Optional.empty();
        return Optional.of(new Microsoft365Client.ConfigM365(
                c.getMsTenantId(), c.getClientId(), cipher.dechiffrer(c.getClientSecretChiffre()),
                c.getSharepointSiteId(), c.getDriveId(), c.getTeamsTeamId(), c.getTeamsChannelId(),
                c.getOutlookExpediteur()));
    }
}
