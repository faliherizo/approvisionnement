package qc.levis.contrat.interop;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import qc.levis.contrat.interop.application.InteropService;
import qc.levis.contrat.interop.application.Jetons;
import qc.levis.contrat.interop.application.dto.IdentifiantsClient;
import qc.levis.contrat.interop.application.repository.ApiClientRepository;
import qc.levis.contrat.interop.application.repository.ApiTokenRepository;
import qc.levis.contrat.interop.domain.ApiClient;
import qc.levis.contrat.interop.domain.ApiToken;

@ExtendWith(MockitoExtension.class)
class InteropServiceTest {

    @Mock ApiClientRepository clients;
    @Mock ApiTokenRepository tokens;
    InteropService service;

    @BeforeEach void avant() { service = new InteropService(clients, tokens); }

    @Test void creer_client_retourne_identifiants_et_stocke_le_hash() {
        when(clients.save(any())).thenAnswer(i -> i.getArgument(0));
        IdentifiantsClient id = service.creerClient(1L, "SIRH", "fiches:ecrire");
        assertTrue(id.clientId().startsWith("cli_"));
        assertNotNull(id.secret());
        // le secret stocké est haché, jamais en clair
        verify(clients).save(argThat(c -> !c.getSecretHash().equals(id.secret()) && Jetons.verifier(id.secret(), c.getSecretHash())));
    }

    @Test void emettre_jeton_valide_les_identifiants() {
        ApiClient c = new ApiClient(1L, "cli_abc", Jetons.hacher("sekret"), "SIRH", null);
        when(clients.findByClientId("cli_abc")).thenReturn(Optional.of(c));
        when(tokens.save(any())).thenAnswer(i -> i.getArgument(0));
        var jeton = service.emettreJeton("cli_abc", "sekret");
        assertEquals("Bearer", jeton.tokenType());
        assertNotNull(jeton.accessToken());
        verify(tokens).save(any());
    }

    @Test void emettre_jeton_refuse_un_mauvais_secret() {
        ApiClient c = new ApiClient(1L, "cli_abc", Jetons.hacher("bon"), "SIRH", null);
        when(clients.findByClientId("cli_abc")).thenReturn(Optional.of(c));
        assertThrows(IllegalArgumentException.class, () -> service.emettreJeton("cli_abc", "mauvais"));
    }

    @Test void authentifier_resout_un_jeton_valide_non_expire() {
        ApiToken t = new ApiToken(1L, "cli_abc", Jetons.hacher("tok"), Instant.now().plusSeconds(3600));
        when(tokens.findByTokenHash(Jetons.hacher("tok"))).thenReturn(Optional.of(t));
        var ctx = service.authentifier("tok");
        assertTrue(ctx.isPresent());
        assertEquals(1L, ctx.get().tenantId());
    }

    @Test void authentifier_rejette_un_jeton_expire() {
        ApiToken t = new ApiToken(1L, "cli_abc", Jetons.hacher("tok"), Instant.now().minusSeconds(1));
        when(tokens.findByTokenHash(Jetons.hacher("tok"))).thenReturn(Optional.of(t));
        assertTrue(service.authentifier("tok").isEmpty());
    }

    @Test void authentifier_vide_ou_null_retourne_vide() {
        assertTrue(service.authentifier(null).isEmpty());
        assertTrue(service.authentifier("  ").isEmpty());
        verifyNoInteractions(tokens);
    }
}
