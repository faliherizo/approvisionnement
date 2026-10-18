package qc.levis.contrat.service;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.commun.application.SecretCipher;
import qc.levis.contrat.integration.application.IntegrationM365Service;
import qc.levis.contrat.integration.application.dto.IntegrationM365Request;
import qc.levis.contrat.integration.application.dto.IntegrationM365Vue;
import qc.levis.contrat.integration.application.port.Microsoft365Client;
import qc.levis.contrat.integration.application.repository.IntegrationM365Repository;
import qc.levis.contrat.integration.domain.IntegrationM365;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Tests unitaires de la configuration M365 par tenant (chiffrement, masquage du secret, activation). */
class IntegrationM365ServiceTest {

    private IntegrationM365Repository repo;
    private IntegrationM365Service service;

    @BeforeEach
    void setUp() {
        repo = mock(IntegrationM365Repository.class);
        service = new IntegrationM365Service(repo, new SecretCipher("cle-de-test!!", "5c0ff33a1b2c3d4e"));
        when(repo.save(any(IntegrationM365.class))).thenAnswer(i -> i.getArgument(0));
    }

    private IntegrationM365Request req(Boolean actif, String secret) {
        return new IntegrationM365Request(actif, "ms-tenant", "client-id", secret,
                "site-1", "drive-1", "team-1", "chan-1", "contrats@ville.ca", "Contrats");
    }

    @Test
    void pour_absent_renvoieConfigInactive() {
        when(repo.findByTenantId(1L)).thenReturn(Optional.empty());
        IntegrationM365 c = service.pour(1L);
        assertFalse(c.isActif());
        assertEquals(1L, c.getTenantId());
    }

    @Test
    void enregistrer_chiffreLeSecret_etVueNeLExposePas() {
        when(repo.findByTenantId(1L)).thenReturn(Optional.empty());

        IntegrationM365Vue vue = service.enregistrer(1L, req(true, "secret-en-clair"));

        assertTrue(vue.actif());
        assertEquals("client-id", vue.clientId());
        assertTrue(vue.secretDefini());
        // La vue est un record sans champ « clientSecret » : impossible de fuiter le secret.
        assertFalse(vue.toString().contains("secret-en-clair"));
    }

    @Test
    void enregistrer_sansNouveauSecret_conserveLExistant() {
        IntegrationM365 existant = new IntegrationM365();
        existant.setTenantId(1L);
        existant.setClientSecretChiffre("DEJA_CHIFFRE");
        when(repo.findByTenantId(1L)).thenReturn(Optional.of(existant));

        service.enregistrer(1L, req(true, "   "));   // secret vide → on garde l'ancien
        assertEquals("DEJA_CHIFFRE", existant.getClientSecretChiffre());
    }

    @Test
    void actifEtConfigure_exigeActifEtIdentifiants() {
        IntegrationM365 c = new IntegrationM365();
        c.setTenantId(1L);
        c.setActif(true);
        when(repo.findByTenantId(1L)).thenReturn(Optional.of(c));
        assertFalse(service.actifEtConfigure(1L));   // actif mais sans identifiants

        c.setMsTenantId("t"); c.setClientId("c"); c.setClientSecretChiffre("x");
        assertTrue(service.actifEtConfigure(1L));

        c.setActif(false);
        assertFalse(service.actifEtConfigure(1L));   // configuré mais inactif
    }

    @Test
    void configClient_actifEtConfigure_renvoieSecretDechiffre() {
        IntegrationM365 c = new IntegrationM365();
        c.setTenantId(1L); c.setActif(true); c.setMsTenantId("t"); c.setClientId("c");
        c.setClientSecretChiffre(new SecretCipher("cle-de-test!!", "5c0ff33a1b2c3d4e").chiffrer("mon-secret"));
        when(repo.findByTenantId(1L)).thenReturn(Optional.of(c));

        Optional<Microsoft365Client.ConfigM365> cfg = service.configClient(1L);
        assertTrue(cfg.isPresent());
        assertEquals("mon-secret", cfg.get().clientSecretClair());
        assertEquals("t", cfg.get().msTenantId());
    }

    @Test
    void configClient_inactif_renvoieVide() {
        when(repo.findByTenantId(1L)).thenReturn(Optional.empty());
        assertTrue(service.configClient(1L).isEmpty());
    }
}
