package qc.levis.contrat.integration.application;

import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.commun.application.SecretCipher;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.integration.application.dto.IntegrationSapRequest;
import qc.levis.contrat.integration.application.dto.IntegrationSapVue;
import qc.levis.contrat.integration.application.repository.IntegrationSapRepository;
import qc.levis.contrat.integration.domain.IntegrationSap;

/**
 * Configuration SAP par tenant (URL du service OData + authentification), gérée depuis l'espace admin.
 * Chiffre les secrets (mot de passe, clé API) au repos et ne les expose jamais en clair. Détermine si
 * un tenant bascule en mode réel (config active et complète) ou reste en simulation.
 */
@Service
public class IntegrationSapService {

    private final IntegrationSapRepository repo;
    private final SecretCipher cipher;

    public IntegrationSapService(IntegrationSapRepository repo, SecretCipher cipher) {
        this.repo = repo;
        this.cipher = cipher;
    }

    public IntegrationSap courant() {
        return pour(TenantContext.get());
    }

    public IntegrationSap pour(Long tenantId) {
        return repo.findByTenantId(tenantId).orElseGet(() -> {
            IntegrationSap vide = new IntegrationSap();
            vide.setTenantId(tenantId);
            return vide;
        });
    }

    /** Vue destinée au frontend : jamais les secrets, seulement l'indication de leur présence. */
    public IntegrationSapVue vue(Long tenantId) {
        return vueDe(pour(tenantId));
    }

    private IntegrationSapVue vueDe(IntegrationSap c) {
        return new IntegrationSapVue(c.isActif(), c.getBaseUrl(), c.getServicePath(),
                c.getAuth(), c.getUsername(),
                c.getPasswordChiffre() != null && !c.getPasswordChiffre().isBlank(),
                c.getApiKeyChiffre() != null && !c.getApiKeyChiffre().isBlank());
    }

    @Transactional
    public IntegrationSapVue enregistrer(Long tenantId, IntegrationSapRequest req) {
        IntegrationSap c = repo.findByTenantId(tenantId).orElseGet(IntegrationSap::new);
        c.setTenantId(tenantId);
        c.setActif(req.actif() != null && req.actif());
        c.setBaseUrl(req.baseUrl());
        c.setServicePath(req.servicePath());
        c.setAuth(req.auth() == null || req.auth().isBlank() ? "basic" : req.auth());
        c.setUsername(req.username());
        // Secrets write-only : ne les écrase que si de nouveaux sont fournis ; sinon conserve.
        if (req.password() != null && !req.password().isBlank()) {
            c.setPasswordChiffre(cipher.chiffrer(req.password()));
        }
        if (req.apiKey() != null && !req.apiKey().isBlank()) {
            c.setApiKeyChiffre(cipher.chiffrer(req.apiKey()));
        }
        return vueDe(repo.save(c));
    }

    /**
     * Configuration à tester, <b>sans exiger l'enregistrement ni l'activation</b> : on valide les accès
     * avant d'activer. Chaque champ vient du formulaire s'il est renseigné, <b>sinon de la config déjà
     * enregistrée</b> du tenant (un test à payload vide teste donc la config sauvegardée). Les secrets
     * write-only laissés vides reprennent ceux stockés. Vide si l'URL de base ou le chemin manque.
     */
    public Optional<ConfigSap> configPourTest(Long tenantId, IntegrationSapRequest req) {
        IntegrationSap stocke = pour(tenantId);
        String baseUrl = premier(req.baseUrl(), stocke.getBaseUrl());
        String servicePath = premier(req.servicePath(), stocke.getServicePath());
        if (estVide(baseUrl) || estVide(servicePath)) return Optional.empty();
        String password = estVide(req.password())
                ? cipher.dechiffrer(stocke.getPasswordChiffre()) : req.password();
        String apiKey = estVide(req.apiKey())
                ? cipher.dechiffrer(stocke.getApiKeyChiffre()) : req.apiKey();
        String auth = premier(req.auth(), stocke.getAuth());
        return Optional.of(new ConfigSap(baseUrl, servicePath, estVide(auth) ? "basic" : auth,
                premier(req.username(), stocke.getUsername()), password, apiKey));
    }

    private boolean estVide(String v) {
        return v == null || v.isBlank();
    }

    /** Première valeur non vide : le formulaire prime, sinon la valeur déjà enregistrée. */
    private String premier(String forme, String stocke) {
        return estVide(forme) ? stocke : forme;
    }

    /** Configuration déchiffrée pour l'adaptateur, si active et complète (sinon vide → simulation). */
    public Optional<ConfigSap> configClient(Long tenantId) {
        IntegrationSap c = pour(tenantId);
        if (!c.isActif() || !c.estConfigure()) return Optional.empty();
        return Optional.of(new ConfigSap(c.getBaseUrl(), c.getServicePath(), c.getAuth(),
                c.getUsername(), cipher.dechiffrer(c.getPasswordChiffre()),
                cipher.dechiffrer(c.getApiKeyChiffre())));
    }

    /** Configuration SAP déchiffrée, prête pour l'appel OData (aucun secret persisté ici). */
    public record ConfigSap(String baseUrl, String servicePath, String auth,
                            String username, String password, String apiKey) {}
}
