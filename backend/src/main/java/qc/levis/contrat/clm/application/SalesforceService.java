package qc.levis.contrat.clm.application;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import qc.levis.contrat.clm.application.dto.ClmDto;
import qc.levis.contrat.clm.application.port.AlertesExploitation;
import qc.levis.contrat.clm.application.port.PortSalesforce;
import qc.levis.contrat.clm.application.port.SourceAleatoire;
import qc.levis.contrat.clm.application.repository.MessageSortantRepository;
import qc.levis.contrat.clm.domain.MessageSortant;
import qc.levis.contrat.clm.domain.Trousse;

/**
 * Intégration Salesforce (ST-06/ST-12, UI-21). Les retours de statut passent par une boîte d'envoi transactionnelle :
 * un changement d'état n'est jamais perdu si Salesforce est indisponible, et jamais publié deux fois.
 */
@Service
public class SalesforceService {

    private static final Logger log = LoggerFactory.getLogger(SalesforceService.class);

    private final PortSalesforce salesforce;
    private final MessageSortantRepository sorties;
    private final SourceAleatoire alea;
    private final ClmProprietes proprietes;
    private final ContexteClm ctx;
    private final AlertesExploitation alertes;
    private volatile ClmDto.PortSalesforceEtat dernierTest;

    public SalesforceService(PortSalesforce salesforce, MessageSortantRepository sorties, SourceAleatoire alea,
                             ClmProprietes proprietes, ContexteClm ctx, AlertesExploitation alertes) {
        this.salesforce = salesforce;
        this.sorties = sorties;
        this.alea = alea;
        this.proprietes = proprietes;
        this.ctx = ctx;
        this.alertes = alertes;
    }

    public PortSalesforce.Dossier lireDossier(String identifiant) {
        if (identifiant == null || identifiant.isBlank()) {
            throw new IllegalArgumentException("Identifiant de demande ou numéro de dossier requis.");
        }
        return salesforce.lireDossier(identifiant.trim())
                .orElseThrow(() -> new NoSuchElementException("Dossier Salesforce introuvable : " + identifiant));
    }

    /**
     * Inscrit le retour de statut d'une trousse issue de Salesforce. La clé d'idempotence combine trousse, événement
     * et séquence du journal : rejouer la même transition ne crée pas de second message.
     */
    public void enfiler(Trousse t, String evenement, long sequence, Map<String, Object> donnees) {
        if (t.getSource() != Trousse.Source.SALESFORCE || t.getSfDossierId() == null) {
            return;
        }
        String cle = t.getTenantId() + ":" + t.getId() + ":" + evenement + ":" + sequence;
        if (sorties.existsByCleIdempotence(cle)) {
            return;
        }
        Map<String, Object> payload = new LinkedHashMap<>(donnees);
        payload.put("reference", t.getReference());
        payload.put("statut", t.getStatut().name());
        sorties.save(new MessageSortant(t.getTenantId(), t.getId(), t.getSfDossierId(), evenement, cle, alea.uuid(),
                ctx.ecrire(payload), ctx.maintenant()));
    }

    /** Relais périodique, tous tenants : chaque message est traité dans le contexte de son tenant. */
    @Transactional
    public int relayer() {
        Instant maintenant = ctx.maintenant();
        int envoyes = 0;
        for (MessageSortant m : sorties.findTop50ByStatutAndProchainEssaiLessThanEqualOrderByProchainEssaiAsc(
                MessageSortant.Statut.EN_ATTENTE, maintenant)) {
            if (publier(m, maintenant)) {
                envoyes++;
            }
        }
        return envoyes;
    }

    private boolean publier(MessageSortant m, Instant maintenant) {
        return ctx.dansTenant(m.getTenantId(), () -> {
            try {
                salesforce.publierStatut(new PortSalesforce.Statut(m.getSfDossierId(), m.getEvenement(), m.getCorrelationId(),
                        m.getCleIdempotence(), ctx.lireObjet(m.getPayloadJson())));
                m.reussir(maintenant);
                return true;
            } catch (RuntimeException e) {
                log.warn("Retour de statut Salesforce en échec (corrélation {}) : {}", m.getCorrelationId(), e.getMessage());
                m.echouer(e.getMessage(), maintenant);
                if (m.getStatut() == MessageSortant.Statut.ECHEC) {
                    alertes.signaler(AlertesExploitation.Code.SALESFORCE_FILE_ERREUR, m.getTenantId(), "corrélation " + m.getCorrelationId(),
                            m.getEvenement() + " · " + m.getTentatives() + " tentatives");
                }
                return false;
            } finally {
                sorties.save(m);
            }
        });
    }

    // --- Administration (UI-21) ---
    public ClmDto.IntegrationVue etat() {
        List<MessageSortant> journal = sorties.findTop100ByTenantIdOrderByCreeLeDesc(ctx.tenant());
        long attente = journal.stream().filter(m -> m.getStatut() == MessageSortant.Statut.EN_ATTENTE).count();
        long echec = journal.stream().filter(m -> m.getStatut() == MessageSortant.Statut.ECHEC).count();
        long envoyes = journal.stream().filter(m -> m.getStatut() == MessageSortant.Statut.ENVOYE).count();
        List<ClmDto.SortieVue> vues = new ArrayList<>();
        journal.forEach(m -> vues.add(vue(m)));
        return new ClmDto.IntegrationVue(dernierTest, attente, echec, envoyes, vues, config());
    }

    ClmDto.ConfigIntegration config() {
        ClmProprietes.Salesforce sf = proprietes.salesforce();
        String id = sf.clientId();
        String masque = id == null || id.isBlank() ? null : id.length() <= 6 ? "••••" : id.substring(0, 4) + "…" + id.substring(id.length() - 2);
        return new ClmDto.ConfigIntegration(sf.mode(), sf.instanceUrl(), sf.objet(), sf.versionApi(), masque,
                sf.clientSecret() != null && !sf.clientSecret().isBlank());
    }

    /** Remet en file les retours en échec d'une trousse (« Relancer intégration », UI-18). */
    @Transactional
    public int relancerTrousse(Long trousseId) {
        Instant maintenant = ctx.maintenant();
        List<MessageSortant> echecs = sorties.findByTenantIdAndTrousseIdInAndStatut(ctx.tenant(), List.of(trousseId), MessageSortant.Statut.ECHEC);
        for (MessageSortant m : echecs) {
            m.relancer(maintenant);
            sorties.save(m);
        }
        return echecs.size();
    }

    public ClmDto.PortSalesforceEtat tester() {
        PortSalesforce.EtatConnexion e = salesforce.tester();
        dernierTest = new ClmDto.PortSalesforceEtat(e.disponible(), e.mode(), e.detail(), ctx.maintenant());
        return dernierTest;
    }

    @Transactional
    public ClmDto.SortieVue relancer(Long id) {
        MessageSortant m = sorties.findByIdAndTenantId(id, ctx.tenant())
                .orElseThrow(() -> new NoSuchElementException("Événement introuvable : " + id));
        m.relancer(ctx.maintenant());
        return vue(sorties.save(m));
    }

    public static ClmDto.SortieVue vue(MessageSortant m) {
        return new ClmDto.SortieVue(m.getId(), m.getTrousseId(), m.getSfDossierId(), m.getEvenement(), m.getCorrelationId(),
                m.getStatut().name(), m.getTentatives(), m.getProchainEssai(), m.getDerniereErreur(), m.getCreeLe(), m.getEnvoyeLe());
    }
}
