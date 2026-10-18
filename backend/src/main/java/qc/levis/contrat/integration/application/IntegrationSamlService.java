package qc.levis.contrat.integration.application;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.integration.application.dto.IntegrationSamlRequest;
import qc.levis.contrat.integration.application.dto.IntegrationSamlVue;
import qc.levis.contrat.integration.application.repository.IntegrationSamlRepository;
import qc.levis.contrat.integration.domain.IntegrationSaml;

/**
 * Configuration d'authentification SAML 2.0 (Service Provider) par tenant, gérée depuis l'espace
 * admin. Détermine si un tenant propose la connexion fédérée SAML (config active et complète).
 * Aucun secret n'est stocké côté SP : le certificat de l'IdP est public et conservé en clair.
 */
@Service
public class IntegrationSamlService {

    private static final Logger log = LoggerFactory.getLogger(IntegrationSamlService.class);

    private final IntegrationSamlRepository repo;

    public IntegrationSamlService(IntegrationSamlRepository repo) {
        this.repo = repo;
    }

    /** Configuration du tenant courant, ou une configuration vierge (inactive) si absente. */
    public IntegrationSaml courant() {
        return pour(TenantContext.get());
    }

    public IntegrationSaml pour(Long tenantId) {
        return repo.findByTenantId(tenantId).orElseGet(() -> {
            IntegrationSaml vide = new IntegrationSaml();
            vide.setTenantId(tenantId);
            return vide;
        });
    }

    /** Vue destinée au frontend : le certificat de l'IdP est public, il est donc renvoyé tel quel. */
    public IntegrationSamlVue vue(Long tenantId) {
        return vueDe(pour(tenantId));
    }

    private IntegrationSamlVue vueDe(IntegrationSaml c) {
        return new IntegrationSamlVue(c.isActif(), c.getIdpEntityId(), c.getIdpSsoUrl(),
                c.getIdpCertificat(), c.getSpEntityId(), c.getClaimCourriel(), c.getBoutonLibelle(),
                c.isAutoProvision(), c.getRoleParDefaut(),
                c.getIdpCertificat() != null && !c.getIdpCertificat().isBlank());
    }

    @Transactional
    public IntegrationSamlVue enregistrer(Long tenantId, IntegrationSamlRequest req) {
        IntegrationSaml c = repo.findByTenantId(tenantId).orElseGet(IntegrationSaml::new);
        c.setTenantId(tenantId);
        c.setActif(req.actif() != null && req.actif());
        c.setIdpEntityId(req.idpEntityId());
        c.setIdpSsoUrl(req.idpSsoUrl());
        c.setSpEntityId(req.spEntityId());
        c.setClaimCourriel(estVide(req.claimCourriel()) ? "email" : req.claimCourriel());
        c.setBoutonLibelle(estVide(req.boutonLibelle()) ? "Connexion SAML" : req.boutonLibelle());
        c.setAutoProvision(req.autoProvision() == null || req.autoProvision());
        c.setRoleParDefaut(estVide(req.roleParDefaut()) ? "REDACTEUR" : req.roleParDefaut());
        // Le certificat de l'IdP est public : on ne l'écrase que si un nouveau est fourni.
        if (!estVide(req.idpCertificat())) {
            c.setIdpCertificat(req.idpCertificat().trim());
        }
        IntegrationSaml sauve = repo.save(c);
        log.info("Configuration SAML enregistrée pour le tenant {} (actif={}, certificat défini={})",
                tenantId, sauve.isActif(), sauve.getIdpCertificat() != null);
        return vueDe(sauve);
    }

    /** Configuration à tester : celle du formulaire fusionnée avec l'enregistrée (le formulaire prime). */
    public IntegrationSaml pourTest(Long tenantId, IntegrationSamlRequest req) {
        IntegrationSaml stocke = pour(tenantId);
        IntegrationSaml effectif = new IntegrationSaml();
        effectif.setIdpEntityId(estVide(req.idpEntityId()) ? stocke.getIdpEntityId() : req.idpEntityId());
        effectif.setIdpSsoUrl(estVide(req.idpSsoUrl()) ? stocke.getIdpSsoUrl() : req.idpSsoUrl());
        effectif.setIdpCertificat(estVide(req.idpCertificat()) ? stocke.getIdpCertificat() : req.idpCertificat());
        effectif.setSpEntityId(estVide(req.spEntityId()) ? stocke.getSpEntityId() : req.spEntityId());
        return effectif;
    }

    /** Configuration pour le flux d'authentification SAML, si active et complète (sinon vide). */
    public Optional<ConfigSaml> configClient(Long tenantId) {
        IntegrationSaml c = pour(tenantId);
        if (!c.isActif() || !c.estConfigure()) return Optional.empty();
        return Optional.of(new ConfigSaml(c.getTenantId(), c.getIdpEntityId(), c.getIdpSsoUrl(),
                c.getIdpCertificat(), c.getSpEntityId(), c.getClaimCourriel(), c.isAutoProvision(),
                c.getRoleParDefaut(), c.getBoutonLibelle()));
    }

    private boolean estVide(String v) {
        return v == null || v.isBlank();
    }

    /** Configuration SAML prête pour le flux SP-initiated (aucun secret : le certificat est public). */
    public record ConfigSaml(Long tenantId, String idpEntityId, String idpSsoUrl, String idpCertificat,
                             String spEntityId, String claimCourriel, boolean autoProvision,
                             String roleParDefaut, String boutonLibelle) {}
}
