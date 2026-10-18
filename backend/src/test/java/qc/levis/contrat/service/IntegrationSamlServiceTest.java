package qc.levis.contrat.service;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.integration.application.IntegrationSamlService;
import qc.levis.contrat.integration.application.dto.IntegrationSamlRequest;
import qc.levis.contrat.integration.application.dto.IntegrationSamlVue;
import qc.levis.contrat.integration.application.repository.IntegrationSamlRepository;
import qc.levis.contrat.integration.domain.IntegrationSaml;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Tests unitaires de la config SAML par tenant (activation, valeurs par défaut, configClient, test). */
class IntegrationSamlServiceTest {

    private static final String CERT = "-----BEGIN CERTIFICATE-----\nMIIB...\n-----END CERTIFICATE-----";

    private IntegrationSamlRepository repo;
    private IntegrationSamlService service;

    @BeforeEach
    void setUp() {
        repo = mock(IntegrationSamlRepository.class);
        service = new IntegrationSamlService(repo);
        when(repo.save(any(IntegrationSaml.class))).thenAnswer(i -> i.getArgument(0));
    }

    private IntegrationSamlRequest req(Boolean actif, String cert) {
        return new IntegrationSamlRequest(actif, "https://idp.test/entity",
                "https://idp.test/sso", cert, "https://sp.levis.ca", null, null, null, null);
    }

    @Test
    void pour_absent_renvoieConfigInactive() {
        when(repo.findByTenantId(1L)).thenReturn(Optional.empty());
        assertFalse(service.pour(1L).isActif());
    }

    @Test
    void enregistrer_appliqueLesDefauts_etVueExposeLeCertificat() {
        when(repo.findByTenantId(1L)).thenReturn(Optional.empty());
        IntegrationSamlVue vue = service.enregistrer(1L, req(true, CERT));

        assertTrue(vue.actif());
        assertEquals("https://idp.test/entity", vue.idpEntityId());
        assertEquals("email", vue.claimCourriel());
        assertEquals("Connexion SAML", vue.boutonLibelle());
        assertTrue(vue.autoProvision());
        assertEquals("REDACTEUR", vue.roleParDefaut());
        assertEquals(CERT, vue.idpCertificat());   // le certificat est public : renvoyé tel quel
        assertTrue(vue.certificatDefini());
    }

    @Test
    void enregistrer_valeursPersonnalisees_sontConservees() {
        when(repo.findByTenantId(1L)).thenReturn(Optional.empty());
        IntegrationSamlRequest req = new IntegrationSamlRequest(true, "eid", "sso", CERT,
                "sp", "urn:courriel", "Connexion ADFS", false, "LECTEUR");
        IntegrationSamlVue vue = service.enregistrer(1L, req);
        assertEquals("urn:courriel", vue.claimCourriel());
        assertEquals("Connexion ADFS", vue.boutonLibelle());
        assertFalse(vue.autoProvision());
        assertEquals("LECTEUR", vue.roleParDefaut());
    }

    @Test
    void enregistrer_sansNouveauCertificat_conserveLExistant() {
        IntegrationSaml existant = new IntegrationSaml();
        existant.setTenantId(1L);
        existant.setIdpCertificat(CERT);
        when(repo.findByTenantId(1L)).thenReturn(Optional.of(existant));

        service.enregistrer(1L, req(true, "  "));
        assertEquals(CERT, existant.getIdpCertificat());
    }

    @Test
    void configClient_actifEtConfigure_renvoieConfig() {
        IntegrationSaml c = new IntegrationSaml();
        c.setTenantId(1L); c.setActif(true);
        c.setIdpEntityId("https://idp.test/entity");
        c.setIdpSsoUrl("https://idp.test/sso");
        c.setIdpCertificat(CERT);
        c.setSpEntityId("https://sp.levis.ca");
        when(repo.findByTenantId(1L)).thenReturn(Optional.of(c));

        var cfg = service.configClient(1L);
        assertTrue(cfg.isPresent());
        assertEquals("https://idp.test/sso", cfg.get().idpSsoUrl());
        assertEquals("https://sp.levis.ca", cfg.get().spEntityId());
        assertEquals(1L, cfg.get().tenantId());
    }

    @Test
    void configClient_inactifOuIncomplet_renvoieVide() {
        when(repo.findByTenantId(1L)).thenReturn(Optional.empty());
        assertTrue(service.configClient(1L).isEmpty());          // absent

        IntegrationSaml actifIncomplet = new IntegrationSaml();
        actifIncomplet.setTenantId(2L); actifIncomplet.setActif(true); // ni SSO ni certificat
        when(repo.findByTenantId(2L)).thenReturn(Optional.of(actifIncomplet));
        assertTrue(service.configClient(2L).isEmpty());
    }

    @Test
    void pourTest_formulairePrime_sinonStocke() {
        IntegrationSaml stocke = new IntegrationSaml();
        stocke.setTenantId(1L);
        stocke.setIdpSsoUrl("https://stocke/sso");
        stocke.setIdpEntityId("https://stocke/entity");
        stocke.setIdpCertificat(CERT);
        when(repo.findByTenantId(1L)).thenReturn(Optional.of(stocke));

        // Formulaire renseigné → il prime.
        IntegrationSaml f = service.pourTest(1L, new IntegrationSamlRequest(null, null,
                "https://forme/sso", null, null, null, null, null, null));
        assertEquals("https://forme/sso", f.getIdpSsoUrl());
        assertEquals("https://stocke/entity", f.getIdpEntityId()); // repris du stocké
        assertEquals(CERT, f.getIdpCertificat());
    }
}
