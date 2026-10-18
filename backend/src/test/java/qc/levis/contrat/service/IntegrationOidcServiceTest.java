package qc.levis.contrat.service;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.commun.application.SecretCipher;
import qc.levis.contrat.integration.application.IntegrationOidcService;
import qc.levis.contrat.integration.application.dto.IntegrationOidcRequest;
import qc.levis.contrat.integration.application.dto.IntegrationOidcVue;
import qc.levis.contrat.integration.application.repository.IntegrationOidcRepository;
import qc.levis.contrat.integration.domain.IntegrationOidc;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Tests unitaires de la config OIDC par tenant (chiffrement, masquage du secret, activation, valeurs par défaut). */
class IntegrationOidcServiceTest {

    private static final String SEL = "5c0ff33a1b2c3d4e";
    private IntegrationOidcRepository repo;
    private IntegrationOidcService service;

    @BeforeEach
    void setUp() {
        repo = mock(IntegrationOidcRepository.class);
        service = new IntegrationOidcService(repo, new SecretCipher("cle-de-test!!", SEL));
        when(repo.save(any(IntegrationOidc.class))).thenAnswer(i -> i.getArgument(0));
    }

    private IntegrationOidcRequest req(Boolean actif, String secret) {
        return new IntegrationOidcRequest(actif, "https://login.microsoftonline.com/t/v2.0",
                "client-123", secret, null, null, null, null, null);
    }

    @Test
    void pour_absent_renvoieConfigInactive() {
        when(repo.findByTenantId(1L)).thenReturn(Optional.empty());
        assertFalse(service.pour(1L).isActif());
    }

    @Test
    void enregistrer_chiffreLeSecret_appliqueLesDefauts_etVueNeLExposePas() {
        when(repo.findByTenantId(1L)).thenReturn(Optional.empty());
        IntegrationOidcVue vue = service.enregistrer(1L, req(true, "tres-secret"));

        assertTrue(vue.actif());
        assertEquals("client-123", vue.clientId());
        assertEquals("openid profile email", vue.scopes());
        assertEquals("Connexion gouvernementale", vue.boutonLibelle());
        assertEquals("email", vue.claimCourriel());
        assertTrue(vue.autoProvision());
        assertEquals("REDACTEUR", vue.roleParDefaut());
        assertTrue(vue.secretDefini());
        assertFalse(vue.toString().contains("tres-secret"));
    }

    @Test
    void enregistrer_valeursPersonnalisees_sontConservees() {
        when(repo.findByTenantId(1L)).thenReturn(Optional.empty());
        IntegrationOidcRequest req = new IntegrationOidcRequest(true,
                "https://idp/realm", "cid", "sec", "openid email", "Connexion Entra",
                "upn", false, "LECTEUR");
        IntegrationOidcVue vue = service.enregistrer(1L, req);
        assertEquals("openid email", vue.scopes());
        assertEquals("Connexion Entra", vue.boutonLibelle());
        assertEquals("upn", vue.claimCourriel());
        assertFalse(vue.autoProvision());
        assertEquals("LECTEUR", vue.roleParDefaut());
    }

    @Test
    void enregistrer_sansNouveauSecret_conserveLExistant() {
        IntegrationOidc existant = new IntegrationOidc();
        existant.setTenantId(1L);
        existant.setClientSecretChiffre("DEJA_CHIFFRE");
        when(repo.findByTenantId(1L)).thenReturn(Optional.of(existant));

        service.enregistrer(1L, req(true, "  "));
        assertEquals("DEJA_CHIFFRE", existant.getClientSecretChiffre());
    }

    @Test
    void configClient_actifEtConfigure_dechiffreLeSecret() {
        IntegrationOidc c = new IntegrationOidc();
        c.setTenantId(1L); c.setActif(true);
        c.setIssuer("https://idp/realm"); c.setClientId("cid");
        c.setClientSecretChiffre(new SecretCipher("cle-de-test!!", SEL).chiffrer("secret-oidc"));
        when(repo.findByTenantId(1L)).thenReturn(Optional.of(c));

        var cfg = service.configClient(1L);
        assertTrue(cfg.isPresent());
        assertEquals("secret-oidc", cfg.get().clientSecret());
        assertEquals("https://idp/realm", cfg.get().issuer());
        assertEquals(1L, cfg.get().tenantId());
    }

    @Test
    void configClient_inactifOuIncomplet_renvoieVide() {
        when(repo.findByTenantId(1L)).thenReturn(Optional.empty());
        assertTrue(service.configClient(1L).isEmpty());   // absent

        IntegrationOidc actifIncomplet = new IntegrationOidc();
        actifIncomplet.setTenantId(2L); actifIncomplet.setActif(true); // ni issuer ni clientId
        when(repo.findByTenantId(2L)).thenReturn(Optional.of(actifIncomplet));
        assertTrue(service.configClient(2L).isEmpty());
    }

    @Test
    void issuerPourTest_formulairePrime_sinonStocke() {
        IntegrationOidc stocke = new IntegrationOidc();
        stocke.setTenantId(1L);
        stocke.setIssuer("https://stocke/issuer");
        when(repo.findByTenantId(1L)).thenReturn(Optional.of(stocke));

        // Formulaire renseigné → il prime.
        assertEquals("https://forme/issuer", service.issuerPourTest(1L,
                new IntegrationOidcRequest(null, "https://forme/issuer", null, null, null, null, null, null, null)));
        // Formulaire vide → on reprend l'émetteur enregistré.
        assertEquals("https://stocke/issuer", service.issuerPourTest(1L,
                new IntegrationOidcRequest(null, null, null, null, null, null, null, null, null)));
    }
}
