package qc.levis.contrat.clm.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Rappel de statut vers le système externe qui a créé la trousse (Workday, Salesforce, autre — addenda 1, TI-22).
 * Écrit dans la transaction du changement de statut, relayé ensuite avec reprises ; la clé d'idempotence empêche
 * un second envoi du même événement, et l'appelant la reçoit pour dédoublonner de son côté.
 */
@Entity
@Table(name = "clm_rappel_sortant", indexes = {
        @Index(name = "ix_clm_rappel_cle", columnList = "cle_idempotence", unique = true),
        @Index(name = "ix_clm_rappel_statut", columnList = "statut,prochain_essai")})
public class RappelSortant {

    public enum Statut { EN_ATTENTE, ENVOYE, ECHEC }

    /** Reprises avant abandon ; l'administrateur peut ensuite relancer à la main. */
    public static final int REPRISES_MAX = 8;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "client_integration_id", nullable = false)
    private Long clientIntegrationId;

    @Column(name = "trousse_id", nullable = false)
    private Long trousseId;

    @Column(nullable = false, length = 40)
    private String evenement;

    @Column(name = "cle_idempotence", nullable = false, length = 160)
    private String cleIdempotence;

    @Column(name = "payload_json", nullable = false, length = 10_000)
    private String payloadJson;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private Statut statut = Statut.EN_ATTENTE;

    @Column(nullable = false)
    private int tentatives;

    @Column(name = "prochain_essai", nullable = false)
    private Instant prochainEssai;

    @Column(name = "derniere_erreur", length = 1000)
    private String derniereErreur;

    @Column(name = "cree_le", nullable = false)
    private Instant creeLe;

    @Column(name = "envoye_le")
    private Instant envoyeLe;

    protected RappelSortant() {
    }

    public RappelSortant(Long tenantId, Long clientIntegrationId, Long trousseId, String evenement, String cleIdempotence,
                         String payloadJson, Instant maintenant) {
        this.tenantId = tenantId;
        this.clientIntegrationId = clientIntegrationId;
        this.trousseId = trousseId;
        this.evenement = evenement;
        this.cleIdempotence = cleIdempotence;
        this.payloadJson = payloadJson;
        this.creeLe = maintenant;
        this.prochainEssai = maintenant;
    }

    public void reussir(Instant maintenant) {
        statut = Statut.ENVOYE;
        envoyeLe = maintenant;
        derniereErreur = null;
    }

    public void echouer(String erreur, Instant maintenant) {
        tentatives++;
        derniereErreur = erreur == null ? null : erreur.substring(0, Math.min(erreur.length(), 1000));
        if (Relances.abandonner(tentatives, REPRISES_MAX)) {
            statut = Statut.ECHEC;
        } else {
            prochainEssai = Relances.prochainEssai(tentatives, maintenant);
        }
    }

    /** Relance manuelle d'un rappel abandonné : il repart pour une série complète de reprises. */
    public void relancer(Instant maintenant) {
        if (statut == Statut.ENVOYE) {
            throw new IllegalStateException("Rappel déjà livré.");
        }
        statut = Statut.EN_ATTENTE;
        tentatives = 0;
        prochainEssai = maintenant;
    }

    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public Long getClientIntegrationId() { return clientIntegrationId; }
    public Long getTrousseId() { return trousseId; }
    public String getEvenement() { return evenement; }
    public String getCleIdempotence() { return cleIdempotence; }
    public String getPayloadJson() { return payloadJson; }
    public Statut getStatut() { return statut; }
    public int getTentatives() { return tentatives; }
    public Instant getProchainEssai() { return prochainEssai; }
    public String getDerniereErreur() { return derniereErreur; }
    public Instant getCreeLe() { return creeLe; }
    public Instant getEnvoyeLe() { return envoyeLe; }
}
