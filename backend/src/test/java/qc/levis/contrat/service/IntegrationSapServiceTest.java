package qc.levis.contrat.service;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.commun.application.SecretCipher;
import qc.levis.contrat.integration.application.IntegrationSapService;
import qc.levis.contrat.integration.application.dto.IntegrationSapRequest;
import qc.levis.contrat.integration.application.dto.IntegrationSapVue;
import qc.levis.contrat.integration.application.repository.IntegrationSapRepository;
import qc.levis.contrat.integration.domain.IntegrationSap;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Tests unitaires de la config SAP par tenant (chiffrement, masquage des secrets, activation). */
class IntegrationSapServiceTest {

    private IntegrationSapRepository repo;
    private IntegrationSapService service;

    @BeforeEach
    void setUp() {
        repo = mock(IntegrationSapRepository.class);
        service = new IntegrationSapService(repo, new SecretCipher("cle-de-test!!", "5c0ff33a1b2c3d4e"));
        when(repo.save(any(IntegrationSap.class))).thenAnswer(i -> i.getArgument(0));
    }

    private IntegrationSapRequest req(Boolean actif, String auth, String pass, String apiKey) {
        return new IntegrationSapRequest(actif, "https://sap.ville.ca/odata", "/ZREF_SRV/References",
                auth, "SAPUSER", pass, apiKey);
    }

    @Test
    void pour_absent_renvoieConfigInactive() {
        when(repo.findByTenantId(1L)).thenReturn(Optional.empty());
        assertFalse(service.pour(1L).isActif());
    }

    @Test
    void enregistrer_chiffreLesSecrets_etVueNeLesExposePas() {
        when(repo.findByTenantId(1L)).thenReturn(Optional.empty());
        IntegrationSapVue vue = service.enregistrer(1L, req(true, "basic", "motDePasse", "cleApi"));

        assertTrue(vue.actif());
        assertEquals("SAPUSER", vue.username());
        assertTrue(vue.passwordDefini());
        assertTrue(vue.apiKeyDefini());
        assertFalse(vue.toString().contains("motDePasse"));
        assertFalse(vue.toString().contains("cleApi"));
    }

    @Test
    void enregistrer_sansNouveauxSecrets_conserveLExistant() {
        IntegrationSap existant = new IntegrationSap();
        existant.setTenantId(1L);
        existant.setPasswordChiffre("DEJA_PWD");
        existant.setApiKeyChiffre("DEJA_KEY");
        when(repo.findByTenantId(1L)).thenReturn(Optional.of(existant));

        service.enregistrer(1L, req(true, "basic", "  ", ""));
        assertEquals("DEJA_PWD", existant.getPasswordChiffre());
        assertEquals("DEJA_KEY", existant.getApiKeyChiffre());
    }

    @Test
    void configClient_actifEtConfigure_dechiffreLeSecret() {
        IntegrationSap c = new IntegrationSap();
        c.setTenantId(1L); c.setActif(true);
        c.setBaseUrl("https://sap/odata"); c.setServicePath("/Ref"); c.setAuth("basic");
        c.setUsername("u");
        c.setPasswordChiffre(new SecretCipher("cle-de-test!!", "5c0ff33a1b2c3d4e").chiffrer("secret"));
        when(repo.findByTenantId(1L)).thenReturn(Optional.of(c));

        var cfg = service.configClient(1L);
        assertTrue(cfg.isPresent());
        assertEquals("secret", cfg.get().password());
        assertEquals("https://sap/odata", cfg.get().baseUrl());
    }

    @Test
    void configClient_inactifOuIncomplet_renvoieVide() {
        when(repo.findByTenantId(1L)).thenReturn(Optional.empty());
        assertTrue(service.configClient(1L).isEmpty());   // absent

        IntegrationSap actifIncomplet = new IntegrationSap();
        actifIncomplet.setTenantId(2L); actifIncomplet.setActif(true); // ni URL ni service
        when(repo.findByTenantId(2L)).thenReturn(Optional.of(actifIncomplet));
        assertTrue(service.configClient(2L).isEmpty());
    }

    @Test
    void configPourTest_formulaireComplet_neExigePasActivationNiEnregistrement() {
        when(repo.findByTenantId(1L)).thenReturn(Optional.empty()); // rien de stocké
        var cfg = service.configPourTest(1L, req(false, "basic", "pwd-saisi", null)); // actif=false
        assertTrue(cfg.isPresent());
        assertEquals("pwd-saisi", cfg.get().password());
        assertEquals("https://sap.ville.ca/odata", cfg.get().baseUrl());
    }

    @Test
    void configPourTest_secretVide_reprendLeSecretStocke() {
        IntegrationSap existant = new IntegrationSap();
        existant.setTenantId(1L);
        existant.setApiKeyChiffre(new SecretCipher("cle-de-test!!", "5c0ff33a1b2c3d4e").chiffrer("cle-stockee"));
        when(repo.findByTenantId(1L)).thenReturn(Optional.of(existant));

        var cfg = service.configPourTest(1L, req(false, "apikey", null, "  ")); // clé laissée vide
        assertTrue(cfg.isPresent());
        assertEquals("cle-stockee", cfg.get().apiKey());
    }

    @Test
    void configPourTest_payloadVide_reprendLaConfigEnregistree() {
        IntegrationSap stocke = new IntegrationSap();
        stocke.setTenantId(1L);
        stocke.setBaseUrl("https://sap.ville.ca/odata"); stocke.setServicePath("/ZREF/References");
        stocke.setAuth("apikey");
        stocke.setApiKeyChiffre(new SecretCipher("cle-de-test!!", "5c0ff33a1b2c3d4e").chiffrer("cle-enregistree"));
        when(repo.findByTenantId(1L)).thenReturn(Optional.of(stocke));

        var vide = new IntegrationSapRequest(null, null, null, null, null, null, null);
        var cfg = service.configPourTest(1L, vide);
        assertTrue(cfg.isPresent());
        assertEquals("https://sap.ville.ca/odata", cfg.get().baseUrl());
        assertEquals("/ZREF/References", cfg.get().servicePath());
        assertEquals("apikey", cfg.get().auth());
        assertEquals("cle-enregistree", cfg.get().apiKey());
    }

    @Test
    void configPourTest_urlOuServiceManquant_renvoieVide() {
        when(repo.findByTenantId(1L)).thenReturn(Optional.empty()); // rien de stocké non plus
        var incomplet = new IntegrationSapRequest(true, "  ", "", "basic", "u", "p", null);
        assertTrue(service.configPourTest(1L, incomplet).isEmpty());
    }

    @Test
    void enregistrer_authVide_defautBasic() {
        when(repo.findByTenantId(1L)).thenReturn(Optional.empty());
        assertEquals("basic", service.enregistrer(1L, req(false, null, "p", null)).auth());
    }
}
