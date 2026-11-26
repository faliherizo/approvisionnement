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
 * Dépôt Microsoft 365 d'une trousse terminée (Annexe 10, TI-01) : document signé ou dossier de preuve vers la bibliothèque
 * désignée, ou avis dans le canal Teams. Inscrit dans la transaction qui termine la trousse, livré ensuite avec reprises ;
 * la clé d'idempotence empêche un second dépôt du même objet, et l'administration relance un dépôt abandonné.
 */
@Entity
@Table(name = "clm_depot_m365", indexes = {
        @Index(name = "ix_clm_depot_m365_cle", columnList = "cle_idempotence", unique = true),
        @Index(name = "ix_clm_depot_m365_statut", columnList = "statut,prochain_essai")})
public class DepotM365 {

    public enum Nature { DOCUMENT_SIGNE, PREUVE, AVIS_TEAMS }

    public enum Statut { EN_ATTENTE, DEPOSE, ECHEC }

    /** Reprises avant abandon ; l'administration peut ensuite relancer à la main. */
    public static final int REPRISES_MAX = 8;
    private static final int ERREUR_MAX = 1000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "trousse_id", nullable = false)
    private Long trousseId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Nature nature;

    /** Document signé visé ; absent pour la preuve et l'avis. */
    @Column(name = "document_id")
    private Long documentId;

    @Column(name = "cle_idempotence", nullable = false, length = 160)
    private String cleIdempotence;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private Statut statut = Statut.EN_ATTENTE;

    @Column(nullable = false)
    private int tentatives;

    @Column(name = "prochain_essai", nullable = false)
    private Instant prochainEssai;

    @Column(name = "derniere_erreur", length = ERREUR_MAX)
    private String derniereErreur;

    @Column(length = 2000)
    private String url;

    @Column(name = "cree_le", nullable = false)
    private Instant creeLe;

    @Column(name = "depose_le")
    private Instant deposeLe;

    protected DepotM365() {
    }

    public DepotM365(Long tenantId, Long trousseId, Nature nature, Long documentId, Instant maintenant) {
        this.tenantId = tenantId;
        this.trousseId = trousseId;
        this.nature = nature;
        this.documentId = documentId;
        this.cleIdempotence = tenantId + ":" + trousseId + ":" + nature + (documentId == null ? "" : ":" + documentId);
        this.creeLe = maintenant;
        this.prochainEssai = maintenant;
    }

    public void reussir(String url, Instant maintenant) {
        statut = Statut.DEPOSE;
        this.url = url;
        deposeLe = maintenant;
        derniereErreur = null;
    }

    public void echouer(String erreur, Instant maintenant) {
        tentatives++;
        derniereErreur = erreur == null ? null : erreur.substring(0, Math.min(erreur.length(), ERREUR_MAX));
        if (Relances.abandonner(tentatives, REPRISES_MAX)) {
            statut = Statut.ECHEC;
        } else {
            prochainEssai = Relances.prochainEssai(tentatives, maintenant);
        }
    }

    /** Relance manuelle (reprise après panne) : le dépôt repart pour une série complète de reprises. */
    public void relancer(Instant maintenant) {
        if (statut == Statut.DEPOSE) {
            throw new IllegalStateException("Dépôt déjà effectué.");
        }
        statut = Statut.EN_ATTENTE;
        tentatives = 0;
        prochainEssai = maintenant;
    }

    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public Long getTrousseId() { return trousseId; }
    public Nature getNature() { return nature; }
    public Long getDocumentId() { return documentId; }
    public String getCleIdempotence() { return cleIdempotence; }
    public Statut getStatut() { return statut; }
    public int getTentatives() { return tentatives; }
    public Instant getProchainEssai() { return prochainEssai; }
    public String getDerniereErreur() { return derniereErreur; }
    public String getUrl() { return url; }
    public Instant getCreeLe() { return creeLe; }
    public Instant getDeposeLe() { return deposeLe; }
}
