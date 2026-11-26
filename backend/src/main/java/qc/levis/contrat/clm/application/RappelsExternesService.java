package qc.levis.contrat.clm.application;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.clm.application.dto.ClmDto;
import qc.levis.contrat.clm.application.port.PortRappel;
import qc.levis.contrat.clm.application.repository.ClientIntegrationRepository;
import qc.levis.contrat.clm.application.repository.RappelSortantRepository;
import qc.levis.contrat.clm.domain.ClientIntegration;
import qc.levis.contrat.clm.domain.RappelSortant;
import qc.levis.contrat.clm.domain.Trousse;

/**
 * Rappels de statut vers le système externe qui a créé une trousse (addenda 1, TI-22 : Workday ; AF-47 : lots
 * Salesforce). Chaque rappel est signé HMAC-SHA256 avec la clé propre au client (en-tête {@code X-CLM-Signature}),
 * porte une clé d'idempotence, et est relayé avec reprises.
 */
@Service
public class RappelsExternesService implements AbonneTransition {

    private static final Logger log = LoggerFactory.getLogger(RappelsExternesService.class);

    private final RappelSortantRepository rappels;
    private final ClientIntegrationRepository clients;
    private final ClientsIntegrationService gestion;
    private final PortRappel port;
    private final ContexteClm ctx;

    public RappelsExternesService(RappelSortantRepository rappels, ClientIntegrationRepository clients,
                                  ClientsIntegrationService gestion, PortRappel port, ContexteClm ctx) {
        this.rappels = rappels;
        this.clients = clients;
        this.gestion = gestion;
        this.port = port;
        this.ctx = ctx;
    }

    /** Inscrit le rappel d'une transition, dans la transaction de cette transition ; sans client à rappeler, rien. */
    public void enfiler(Trousse t, String evenement, long sequence, Map<String, Object> donnees) {
        if (t.getClientIntegrationId() == null) {
            return;
        }
        ClientIntegration c = clients.findById(t.getClientIntegrationId()).orElse(null);
        if (c == null || !c.rappelConfigure()) {
            return;
        }
        String cle = t.getTenantId() + ":" + t.getId() + ":" + evenement + ":" + sequence;
        if (rappels.existsByCleIdempotence(cle)) {
            return;
        }
        Map<String, Object> corps = new LinkedHashMap<>();
        corps.put("evenement", evenement);
        corps.put("cleIdempotence", cle);
        corps.put("trousseId", t.getId());
        corps.put("reference", t.getReference());
        corps.put("referenceExterne", t.getReferenceExterne());
        corps.put("statut", t.getStatut().name());
        corps.put("horodatage", ctx.maintenant().toString());
        corps.put("donnees", donnees);
        rappels.save(new RappelSortant(t.getTenantId(), c.getId(), t.getId(), evenement, cle, ctx.ecrire(corps), ctx.maintenant()));
    }

    /** Relais périodique, tous tenants ; retourne le nombre de rappels livrés. */
    @Transactional
    public int relayer() {
        Instant maintenant = ctx.maintenant();
        int livres = 0;
        for (RappelSortant r : rappels.findTop50ByStatutAndProchainEssaiLessThanEqualOrderByProchainEssaiAsc(RappelSortant.Statut.EN_ATTENTE, maintenant)) {
            livres += ctx.dansTenant(r.getTenantId(), () -> livrer(r, maintenant));
        }
        return livres;
    }

    private int livrer(RappelSortant r, Instant maintenant) {
        ClientIntegration c = clients.findById(r.getClientIntegrationId()).orElse(null);
        if (c == null || !c.isActif() || !c.rappelConfigure()) {
            r.echouer("Client d'intégration révoqué ou sans adresse de rappel.", maintenant);
            rappels.save(r);
            return 0;
        }
        try {
            port.envoyer(c.getUrlRappel(), r.getPayloadJson(), signer(gestion.cleRappel(c), r.getPayloadJson()), r.getCleIdempotence());
            r.reussir(maintenant);
            rappels.save(r);
            return 1;
        } catch (PortRappel.Refuse e) {
            log.warn("Rappel {} vers le client d'intégration {} non livré : {}", r.getId(), c.getClientId(), e.getMessage());
            r.echouer(e.getMessage(), maintenant);
            rappels.save(r);
            return 0;
        }
    }

    public List<ClmDto.RappelVue> lister() {
        return rappels.findTop100ByTenantIdOrderByCreeLeDesc(ctx.tenant()).stream().map(this::vue).toList();
    }

    @Transactional
    public ClmDto.RappelVue relancer(Long id) {
        RappelSortant r = rappels.findByIdAndTenantId(id, ctx.tenant()).orElseThrow(() -> new NoSuchElementException("Rappel introuvable."));
        r.relancer(ctx.maintenant());
        return vue(rappels.save(r));
    }

    /** Signature lue par le destinataire : {@code sha256=} suivi du HMAC hexadécimal du corps exact reçu. */
    static String signer(String cle, String corps) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(cle.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return "sha256=" + HexFormat.of().formatHex(mac.doFinal(corps.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HMAC-SHA256 indisponible.", e);
        }
    }

    private ClmDto.RappelVue vue(RappelSortant r) {
        String client = clients.findById(r.getClientIntegrationId()).map(ClientIntegration::getNom).orElse("—");
        return new ClmDto.RappelVue(r.getId(), client, r.getTrousseId(), r.getEvenement(), r.getStatut().name(), r.getTentatives(),
                r.getDerniereErreur(), r.getCreeLe(), r.getEnvoyeLe());
    }
}
