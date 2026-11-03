package qc.levis.contrat.interop.application;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.interop.application.dto.ContexteApi;
import qc.levis.contrat.interop.application.dto.IdentifiantsClient;
import qc.levis.contrat.interop.application.dto.JetonEmis;
import qc.levis.contrat.interop.application.repository.ApiClientRepository;
import qc.levis.contrat.interop.application.repository.ApiTokenRepository;
import qc.levis.contrat.interop.domain.ApiClient;
import qc.levis.contrat.interop.domain.ApiToken;

/**
 * API publique : émission/vérification de jetons d'accès (OAuth2 client_credentials simplifié).
 * Les secrets et jetons ne sont stockés que HACHÉS ({@link Jetons}).
 */
@Service
public class InteropService {

    private static final long DUREE_JETON_H = 1;

    private final ApiClientRepository clients;
    private final ApiTokenRepository tokens;

    public InteropService(ApiClientRepository clients, ApiTokenRepository tokens) {
        this.clients = clients;
        this.tokens = tokens;
    }

    @Transactional
    public IdentifiantsClient creerClient(Long tenantId, String nom, String scopes) {
        String clientId = "cli_" + Jetons.generer().substring(0, 16);
        String secret = Jetons.generer();
        clients.save(new ApiClient(tenantId, clientId, Jetons.hacher(secret), nom, scopes));
        return new IdentifiantsClient(clientId, secret);
    }

    public List<ApiClient> listerClients(Long tenantId) {
        return clients.findByTenantId(tenantId);
    }

    @Transactional
    public JetonEmis emettreJeton(String clientId, String secret) {
        ApiClient c = clients.findByClientId(clientId)
                .filter(ApiClient::isActif)
                .filter(x -> Jetons.verifier(secret, x.getSecretHash()))
                .orElseThrow(() -> new IllegalArgumentException("Identifiants client invalides."));
        String token = Jetons.generer();
        Instant exp = Instant.now().plus(DUREE_JETON_H, ChronoUnit.HOURS);
        tokens.save(new ApiToken(c.getTenantId(), c.getClientId(), Jetons.hacher(token), exp));
        return new JetonEmis(token, "Bearer", exp);
    }

    /** Résout un jeton opaque en contexte (tenant + client) s'il est valide et non expiré. */
    public Optional<ContexteApi> authentifier(String token) {
        if (token == null || token.isBlank()) return Optional.empty();
        return tokens.findByTokenHash(Jetons.hacher(token.trim()))
                .filter(t -> t.getExpireLe().isAfter(Instant.now()))
                .map(t -> new ContexteApi(t.getTenantId(), t.getClientId()));
    }
}
