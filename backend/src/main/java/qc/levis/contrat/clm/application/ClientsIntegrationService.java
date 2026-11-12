package qc.levis.contrat.clm.application;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.clm.application.dto.ClmDto;
import qc.levis.contrat.clm.application.port.Scelleur;
import qc.levis.contrat.clm.application.port.SourceAleatoire;
import qc.levis.contrat.clm.application.repository.ClientIntegrationRepository;
import qc.levis.contrat.clm.domain.ClientIntegration;
import qc.levis.contrat.commun.application.SecretCipher;

/**
 * Clients d'intégration de l'organisation (addenda 1) : Salesforce, Workday ou tout autre système autorisé à déclencher
 * des signatures par l'API. Le jeton d'accès (OAuth 2.0 client credentials) est scellé et sans état ; il est refusé dès
 * que son client est révoqué.
 */
@Service
public class ClientsIntegrationService {

    static final Duration DUREE_JETON = Duration.ofHours(1);
    private static final String SEPARATEUR = "|";

    private final ClientIntegrationRepository clients;
    private final SourceAleatoire alea;
    private final SecretCipher chiffrement;
    private final Scelleur scelleur;
    private final JournalClm journal;
    private final ContexteClm ctx;

    public ClientsIntegrationService(ClientIntegrationRepository clients, SourceAleatoire alea, SecretCipher chiffrement,
                                     Scelleur scelleur, JournalClm journal, ContexteClm ctx) {
        this.clients = clients;
        this.alea = alea;
        this.chiffrement = chiffrement;
        this.scelleur = scelleur;
        this.journal = journal;
        this.ctx = ctx;
    }

    /** Refus d'authentification d'un système externe : jamais de détail sur la cause (client inconnu, secret, révocation). */
    public static class AccesRefuse extends RuntimeException {
        public AccesRefuse() {
            super("Client d'intégration non authentifié.");
        }
    }

    public List<ClmDto.ClientIntegrationVue> lister() {
        return clients.findByTenantIdOrderByCreeLeDesc(ctx.tenant()).stream().map(ClientsIntegrationService::vue).toList();
    }

    @Transactional
    public ClmDto.ClientIntegrationCree creer(ClmDto.ClientIntegrationRequest req, String acteur) {
        String secret = alea.jeton();
        String cleRappel = alea.jeton();
        ClientIntegration c = clients.save(ClientIntegration.creer(ctx.tenant(), req.nom(), systeme(req.systeme()), req.urlRappel(),
                "clm-" + alea.uuid(), empreinte(secret), chiffrement.chiffrer(cleRappel), acteur, ctx.maintenant()));
        journal.configuration(acteur, "CLIENT_INTEGRATION", "créé : " + c.getNom() + " (" + c.getSysteme() + ", " + c.getClientId() + ")");
        return new ClmDto.ClientIntegrationCree(vue(c), secret, cleRappel);
    }

    @Transactional
    public ClmDto.ClientIntegrationVue revoquer(Long id, String acteur) {
        ClientIntegration c = clients.findByIdAndTenantId(id, ctx.tenant())
                .orElseThrow(() -> new NoSuchElementException("Client d'intégration introuvable."));
        c.revoquer(ctx.maintenant());
        journal.configuration(acteur, "CLIENT_INTEGRATION", "révoqué : " + c.getNom() + " (" + c.getClientId() + ")");
        return vue(clients.save(c));
    }

    /** Échange client credentials → jeton d'accès d'une heure. */
    public ClmDto.JetonAcces emettreJeton(String clientId, String secret) {
        ClientIntegration c = Optional.ofNullable(clientId).flatMap(clients::findByClientId)
                .filter(x -> secret != null && x.authentifie(empreinte(secret))).orElseThrow(AccesRefuse::new);
        String charge = c.getClientId() + SEPARATEUR + ctx.maintenant().plus(DUREE_JETON).getEpochSecond();
        String jeton = Base64.getUrlEncoder().withoutPadding().encodeToString(charge.getBytes(StandardCharsets.UTF_8))
                + "." + scelleur.sceller(charge.getBytes(StandardCharsets.UTF_8));
        return new ClmDto.JetonAcces(jeton, "Bearer", DUREE_JETON.toSeconds());
    }

    /** Jeton intact, non expiré, d'un client toujours actif ; sinon {@link AccesRefuse}. */
    public ClientIntegration authentifier(String jeton) {
        if (jeton == null || jeton.indexOf('.') < 1) {
            throw new AccesRefuse();
        }
        String[] parties = jeton.split("\\.", 2);
        byte[] charge;
        try {
            charge = Base64.getUrlDecoder().decode(parties[0]);
        } catch (IllegalArgumentException e) {
            throw new AccesRefuse();
        }
        if (!scelleur.verifier(charge, parties[1])) {
            throw new AccesRefuse();
        }
        String[] champs = new String(charge, StandardCharsets.UTF_8).split("\\" + SEPARATEUR);
        if (champs.length != 2 || Instant.ofEpochSecond(Long.parseLong(champs[1])).isBefore(ctx.maintenant())) {
            throw new AccesRefuse();
        }
        return clients.findByClientId(champs[0]).filter(ClientIntegration::isActif).orElseThrow(AccesRefuse::new);
    }

    /** Empreinte à clé du secret : sceau HMAC du serveur, jamais le secret lui-même. */
    private String empreinte(String secret) {
        return scelleur.sceller(("client-integration:" + secret).getBytes(StandardCharsets.UTF_8));
    }

    /** Clé qui signe les rappels de ce client, déchiffrée pour l'envoi seulement. */
    String cleRappel(ClientIntegration c) {
        return chiffrement.dechiffrer(c.getCleRappelChiffree());
    }

    private static ClientIntegration.Systeme systeme(String saisi) {
        try {
            return ClientIntegration.Systeme.valueOf(saisi == null ? "" : saisi.strip().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Système d'origine : SALESFORCE, WORKDAY ou AUTRE.", e);
        }
    }

    static ClmDto.ClientIntegrationVue vue(ClientIntegration c) {
        return new ClmDto.ClientIntegrationVue(c.getId(), c.getNom(), c.getSysteme().name(), c.getClientId(), c.getUrlRappel(),
                c.isActif(), c.getCreePar(), c.getCreeLe(), c.getRevoqueLe());
    }
}
