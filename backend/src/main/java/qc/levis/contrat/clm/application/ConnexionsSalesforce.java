package qc.levis.contrat.clm.application;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.clm.application.dto.ClmDto;
import qc.levis.contrat.clm.application.port.PorteeTenant;
import qc.levis.contrat.clm.application.repository.ConnexionSalesforceRepository;
import qc.levis.contrat.clm.domain.ConnexionSalesforce;
import qc.levis.contrat.clm.domain.Relances;
import qc.levis.contrat.commun.application.SecretCipher;

/**
 * Connexion Salesforce de chaque organisation (UI-21) : la sienne si elle l'a configurée, sinon celle de la plateforme.
 * Le secret est chiffré au repos, ne revient jamais vers l'interface et n'est déchiffré que pour l'adaptateur.
 */
@Service
public class ConnexionsSalesforce {

    public static final ConnexionSalesforce.ChampsRetour CHAMPS_PAR_DEFAUT =
            new ConnexionSalesforce.ChampsRetour("CLM_Statut__c", "CLM_Reference__c", "CLM_Correlation__c");

    /** Connexion effective ; {@code proprietaire} nul : celle de la plateforme, partagée par les organisations sans la leur. */
    public record Connexion(Long proprietaire, ConnexionSalesforce.Mode mode, String instanceUrl, String tokenUrl, String clientId,
                            String clientSecret, String objet, String versionApi, ConnexionSalesforce.ChampsRetour champs,
                            int reprisesMax) {

        /** Jamais le secret dans un journal ni une trace d'erreur. */
        @Override
        public String toString() {
            return "Connexion[" + (proprietaire == null ? "plateforme" : "organisation " + proprietaire) + ", " + mode + ", "
                    + instanceUrl + ", " + objet + ", " + versionApi + "]";
        }
    }

    private final ConnexionSalesforceRepository connexions;
    private final ClmProprietes proprietes;
    private final SecretCipher chiffrement;
    private final PorteeTenant portee;
    private final JournalClm journal;
    private final ContexteClm ctx;

    public ConnexionsSalesforce(ConnexionSalesforceRepository connexions, ClmProprietes proprietes, SecretCipher chiffrement,
                                PorteeTenant portee, JournalClm journal, ContexteClm ctx) {
        this.connexions = connexions;
        this.proprietes = proprietes;
        this.chiffrement = chiffrement;
        this.portee = portee;
        this.journal = journal;
        this.ctx = ctx;
    }

    /** Connexion de l'organisation en cours de traitement (requête, portail ou relais planifié). */
    public Connexion courante() {
        Long tenant = portee.courant();
        return propre(tenant).map(this::connexion).orElseGet(this::plateforme);
    }

    private Optional<ConnexionSalesforce> propre(Long tenant) {
        return tenant == null ? Optional.empty() : connexions.findByTenantId(tenant);
    }

    private Connexion plateforme() {
        ClmProprietes.Salesforce sf = proprietes.salesforce();
        return new Connexion(null, modePlateforme(), sf.instanceUrl(), sf.tokenUrl(), sf.clientId(), sf.clientSecret(), sf.objet(),
                sf.versionApi(), CHAMPS_PAR_DEFAUT, Relances.REPRISES_PAR_DEFAUT);
    }

    private Connexion connexion(ConnexionSalesforce c) {
        ConnexionSalesforce.Reglages r = c.reglages();
        return new Connexion(c.getTenantId(), r.mode(), r.instanceUrl(), r.tokenUrl(), r.clientId(),
                c.secretConfigure() ? chiffrement.dechiffrer(c.getSecretChiffre()) : null, r.objet(), r.versionApi(), r.champs(),
                r.reprisesMax());
    }

    /** Mode saisi (casse indifférente) ; nul s'il n'est ni SIMULATION ni REST. */
    static ConnexionSalesforce.Mode mode(String saisi) {
        String m = saisi == null ? "" : saisi.trim().toUpperCase(Locale.ROOT);
        return switch (m) {
            case "REST" -> ConnexionSalesforce.Mode.REST;
            case "SIMULATION" -> ConnexionSalesforce.Mode.SIMULATION;
            default -> null;
        };
    }

    /** La plateforme simule Salesforce tant que son déploiement ne désigne pas l'API réelle. */
    private ConnexionSalesforce.Mode modePlateforme() {
        ConnexionSalesforce.Mode m = mode(proprietes.salesforce().mode());
        return m == null ? ConnexionSalesforce.Mode.SIMULATION : m;
    }

    // --- Administration (UI-21) ---

    public ClmDto.ConfigIntegration configuration() {
        Optional<ConnexionSalesforce> propre = connexions.findByTenantId(ctx.tenant());
        if (propre.isPresent()) {
            ConnexionSalesforce c = propre.get();
            return vue(c.reglages(), c.secretConfigure(), c.getModifiePar(), c.getModifieLe(), true);
        }
        ClmProprietes.Salesforce sf = proprietes.salesforce();
        ConnexionSalesforce.Reglages r = new ConnexionSalesforce.Reglages(modePlateforme(), sf.instanceUrl(), sf.tokenUrl(), sf.clientId(),
                sf.objet(), sf.versionApi(), CHAMPS_PAR_DEFAUT, Relances.REPRISES_PAR_DEFAUT);
        return vue(r, sf.clientSecret() != null && !sf.clientSecret().isBlank(), null, null, false);
    }

    @Transactional
    public ClmDto.ConfigIntegration modifier(ClmDto.ConnexionRequest req, String acteur) {
        Long tenant = ctx.tenant();
        Optional<ConnexionSalesforce> existante = connexions.findByTenantId(tenant);
        ConnexionSalesforce c = existante.orElseGet(() -> new ConnexionSalesforce(tenant));
        ConnexionSalesforce.Reglages r = fusion(req, existante.map(x -> x.reglages().clientId()).orElse(null));
        String secret = req.clientSecret() == null || req.clientSecret().isBlank() ? null : req.clientSecret().trim();
        List<String> erreurs = new ArrayList<>(r.erreurs(proprietes.salesforce().hotesAutorises(), secret != null || c.secretConfigure()));
        if (proprietes.production() && r.mode() == ConnexionSalesforce.Mode.SIMULATION) {
            erreurs.add("Le mode SIMULATION est interdit en production.");
        }
        if (!erreurs.isEmpty()) {
            throw new IllegalArgumentException(String.join(" ", erreurs));
        }
        c.appliquer(r, secret == null ? null : chiffrement.chiffrer(secret), acteur, ctx.maintenant());
        connexions.save(c);
        journal.configuration(acteur, "SALESFORCE", r.mode() + " · " + (r.instanceUrl() == null ? "-" : r.instanceUrl()) + " · "
                + r.objet() + " " + r.versionApi() + " · retours " + r.champs().statut() + "/" + r.champs().reference() + "/"
                + r.champs().correlation() + " · reprises " + r.reprisesMax() + (secret == null ? "" : " · secret remplacé"));
        return configuration();
    }

    /** L'organisation revient à la connexion de la plateforme ; son secret est effacé avec sa configuration. */
    @Transactional
    public ClmDto.ConfigIntegration retablir(String acteur) {
        connexions.findByTenantId(ctx.tenant()).ifPresent(c -> {
            connexions.delete(c);
            journal.configuration(acteur, "SALESFORCE", "connexion de la plateforme rétablie");
        });
        return configuration();
    }

    /** L'identifiant client n'est jamais renvoyé en clair : laissé vide, celui de l'organisation est conservé. */
    private static ConnexionSalesforce.Reglages fusion(ClmDto.ConnexionRequest req, String clientIdEnPlace) {
        String clientId = nettoye(req.clientId());
        return new ConnexionSalesforce.Reglages(mode(req.mode()), nettoye(req.instanceUrl()), nettoye(req.tokenUrl()),
                clientId == null ? clientIdEnPlace : clientId, nettoye(req.objet()), nettoye(req.versionApi()),
                new ConnexionSalesforce.ChampsRetour(nettoye(req.champStatut()), nettoye(req.champReference()), nettoye(req.champCorrelation())),
                req.reprisesMax() == null ? Relances.REPRISES_PAR_DEFAUT : req.reprisesMax());
    }

    private static String nettoye(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private ClmDto.ConfigIntegration vue(ConnexionSalesforce.Reglages r, boolean secretConfigure, String modifiePar, Instant modifieLe,
                                         boolean personnalisee) {
        return new ClmDto.ConfigIntegration(r.mode().name(), r.instanceUrl(), r.tokenUrl(), r.objet(), r.versionApi(), masque(r.clientId()),
                secretConfigure, r.champs().statut(), r.champs().reference(), r.champs().correlation(), r.reprisesMax(), personnalisee,
                modifiePar, modifieLe, proprietes.salesforce().hotesAutorises());
    }

    static String masque(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        return id.length() <= 6 ? "••••" : id.substring(0, 4) + "…" + id.substring(id.length() - 2);
    }
}
