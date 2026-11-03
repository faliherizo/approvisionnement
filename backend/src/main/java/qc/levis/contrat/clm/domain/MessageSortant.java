package qc.levis.contrat.clm.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Boîte d'envoi vers Salesforce (ST-12) : écrite dans la même transaction que le changement de statut, relayée
 * ensuite avec reprises. La clé d'idempotence unique empêche qu'un même événement soit publié deux fois.
 */
@Entity
@Table(name = "clm_sortie", indexes = {
        @Index(name = "ix_clm_sortie_cle", columnList = "cle_idempotence", unique = true),
        @Index(name = "ix_clm_sortie_statut", columnList = "statut,prochain_essai")})
public class MessageSortant {

    public enum Statut { EN_ATTENTE, ENVOYE, ECHEC }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "trousse_id", nullable = false)
    private Long trousseId;

    @Column(name = "sf_dossier_id", nullable = false, length = 80)
    private String sfDossierId;

    @Column(nullable = false, length = 40)
    private String evenement;

    @Column(name = "cle_idempotence", nullable = false, length = 160)
    private String cleIdempotence;

    @Column(name = "correlation_id", nullable = false, length = 36)
    private String correlationId;

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

    protected MessageSortant() {
    }

    public MessageSortant(Long tenantId, Long trousseId, String sfDossierId, String evenement, String cleIdempotence,
                          String correlationId, String payloadJson, Instant maintenant) {
        this.tenantId = tenantId;
        this.trousseId = trousseId;
        this.sfDossierId = sfDossierId;
        this.evenement = evenement;
        this.cleIdempotence = cleIdempotence;
        this.correlationId = correlationId;
        this.payloadJson = payloadJson;
        this.creeLe = maintenant;
        this.prochainEssai = maintenant;
    }

    public void reussir(Instant maintenant) {
        this.statut = Statut.ENVOYE;
        this.envoyeLe = maintenant;
        this.derniereErreur = null;
    }

    public void echouer(String erreur, Instant maintenant) {
        this.tentatives++;
        this.derniereErreur = erreur == null ? null : erreur.substring(0, Math.min(erreur.length(), 1000));
        if (Relances.abandonner(tentatives)) {
            this.statut = Statut.ECHEC;
        } else {
            this.prochainEssai = Relances.prochainEssai(tentatives, maintenant);
        }
    }

    /** Relance manuelle depuis l'administration (UI-21) : un message déjà envoyé n'est jamais republié. */
    public void relancer(Instant maintenant) {
        if (statut == Statut.ENVOYE) {
            throw new IllegalStateException("Événement dupliqué : déjà transmis.");
        }
        this.statut = Statut.EN_ATTENTE;
        this.tentatives = 0;
        this.prochainEssai = maintenant;
    }

    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public Long getTrousseId() { return trousseId; }
    public String getSfDossierId() { return sfDossierId; }
    public String getEvenement() { return evenement; }
    public String getCleIdempotence() { return cleIdempotence; }
    public String getCorrelationId() { return correlationId; }
    public String getPayloadJson() { return payloadJson; }
    public Statut getStatut() { return statut; }
    public int getTentatives() { return tentatives; }
    public Instant getProchainEssai() { return prochainEssai; }
    public String getDerniereErreur() { return derniereErreur; }
    public Instant getCreeLe() { return creeLe; }
    public Instant getEnvoyeLe() { return envoyeLe; }
}
