package qc.levis.contrat.integration.application;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.commun.application.SecretCipher;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.integration.application.dto.IntegrationOidcRequest;
import qc.levis.contrat.integration.application.dto.IntegrationOidcVue;
import qc.levis.contrat.integration.application.repository.IntegrationOidcRepository;
import qc.levis.contrat.integration.domain.IntegrationOidc;

/**
 * Configuration d'authentification OpenID Connect par tenant (Microsoft Entra ID ou tout IdP OIDC),
 * gérée depuis l'espace admin. Chiffre le secret client au repos et ne l'expose jamais en clair.
 * Détermine si un tenant propose la connexion gouvernementale (config active et complète).
 */
@Service
public class IntegrationOidcService {

    private static final Logger log = LoggerFactory.getLogger(IntegrationOidcService.class);

    private final IntegrationOidcRepository repo;
    private final SecretCipher cipher;

    public IntegrationOidcService(IntegrationOidcRepository repo, SecretCipher cipher) {
        this.repo = repo;
        this.cipher = cipher;
    }

    /** Configuration du tenant courant, ou une configuration vierge (inactive) si absente. */
    public IntegrationOidc courant() {
        return pour(TenantContext.get());
    }

    public IntegrationOidc pour(Long tenantId) {
        return repo.findByTenantId(tenantId).orElseGet(() -> {
            IntegrationOidc vide = new IntegrationOidc();
            vide.setTenantId(tenantId);
            return vide;
        });
    }

    /** Vue destinée au frontend : jamais le secret, seulement l'indication de sa présence. */
    public IntegrationOidcVue vue(Long tenantId) {
        return vueDe(pour(tenantId));
    }

    private IntegrationOidcVue vueDe(IntegrationOidc c) {
        return new IntegrationOidcVue(c.isActif(), c.getIssuer(), c.getClientId(), c.getScopes(),
                c.getBoutonLibelle(), c.getClaimCourriel(), c.isAutoProvision(), c.getRoleParDefaut(),
                c.getClientSecretChiffre() != null && !c.getClientSecretChiffre().isBlank());
    }

    @Transactional
    public IntegrationOidcVue enregistrer(Long tenantId, IntegrationOidcRequest req) {
        IntegrationOidc c = repo.findByTenantId(tenantId).orElseGet(IntegrationOidc::new);
        c.setTenantId(tenantId);
        c.setActif(req.actif() != null && req.actif());
        c.setIssuer(req.issuer());
        c.setClientId(req.clientId());
        c.setScopes(estVide(req.scopes()) ? "openid profile email" : req.scopes());
        c.setBoutonLibelle(estVide(req.boutonLibelle()) ? "Connexion gouvernementale" : req.boutonLibelle());
        c.setClaimCourriel(estVide(req.claimCourriel()) ? "email" : req.claimCourriel());
        c.setAutoProvision(req.autoProvision() == null || req.autoProvision());
        c.setRoleParDefaut(estVide(req.roleParDefaut()) ? "REDACTEUR" : req.roleParDefaut());
        // Secret write-only : ne l'écrase que si un nouveau est fourni ; sinon conserve l'existant.
        if (!estVide(req.clientSecret())) {
            c.setClientSecretChiffre(cipher.chiffrer(req.clientSecret()));
        }
        IntegrationOidc sauve = repo.save(c);
        // On journalise le tenant et l'état, JAMAIS le secret ni l'identifiant client.
        log.info("Configuration OIDC enregistrée pour le tenant {} (actif={}, secret défini={})",
                tenantId, sauve.isActif(), sauve.getClientSecretChiffre() != null);
        return vueDe(sauve);
    }

    /** Émetteur à tester : celui du formulaire s'il est renseigné, sinon celui déjà enregistré. */
    public String issuerPourTest(Long tenantId, IntegrationOidcRequest req) {
        return estVide(req.issuer()) ? pour(tenantId).getIssuer() : req.issuer();
    }

    /** Configuration déchiffrée pour le flux d'authentification, si active et complète (sinon vide). */
    public Optional<ConfigOidc> configClient(Long tenantId) {
        IntegrationOidc c = pour(tenantId);
        if (!c.isActif() || !c.estConfigure()) return Optional.empty();
        return Optional.of(new ConfigOidc(c.getTenantId(), c.getIssuer(), c.getClientId(),
                cipher.dechiffrer(c.getClientSecretChiffre()), c.getScopes(), c.getClaimCourriel(),
                c.isAutoProvision(), c.getRoleParDefaut(), c.getBoutonLibelle()));
    }

    private boolean estVide(String v) {
        return v == null || v.isBlank();
    }

    /** Configuration OIDC déchiffrée, prête pour le flux authorization code (aucun secret persisté ici). */
    public record ConfigOidc(Long tenantId, String issuer, String clientId, String clientSecret,
                             String scopes, String claimCourriel, boolean autoProvision,
                             String roleParDefaut, String boutonLibelle) {}
}
