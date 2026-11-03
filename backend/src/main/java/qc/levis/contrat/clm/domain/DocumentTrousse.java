package qc.levis.contrat.clm.domain;

import java.time.Instant;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/**
 * Une version d'un document de trousse (ST-04, UI-05/UI-06). Les versions d'un même document partagent
 * {@code cleDocument} ; une version signée est verrouillée et toute correction passe par une nouvelle version.
 */
@Entity
@Table(name = "clm_document", indexes = @Index(name = "ix_clm_doc_trousse", columnList = "tenant_id,trousse_id"))
public class DocumentTrousse {

    public enum Statut { BROUILLON, ENVOYE, SIGNE, REMPLACE, RETIRE }

    public enum Analyse { EN_ATTENTE, PROPRE, INFECTE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "trousse_id", nullable = false)
    private Long trousseId;

    @Column(name = "cle_document", nullable = false, length = 40)
    private String cleDocument;

    @Column(nullable = false)
    private int version;

    @Column(nullable = false, length = 200)
    private String nom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private FormatDocument.Type type;

    @Column(nullable = false)
    private long taille;

    @Column(nullable = false, length = 64)
    private String empreinte;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private Statut statut = Statut.BROUILLON;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private Analyse analyse = Analyse.EN_ATTENTE;

    @Column(nullable = false, length = 150)
    private String auteur;

    @Column(length = 500)
    private String motif;

    @Basic(fetch = FetchType.LAZY)
    @Column(name = "contenu", nullable = false)
    private byte[] contenu;

    @Column(name = "cree_le", nullable = false)
    private Instant creeLe;

    protected DocumentTrousse() {
    }

    public DocumentTrousse(Long tenantId, Long trousseId, String cleDocument, int version, String nom, FormatDocument.Type type,
                           byte[] contenu, String auteur, String motif, Instant maintenant) {
        this.tenantId = tenantId;
        this.trousseId = trousseId;
        this.cleDocument = cleDocument;
        this.version = version;
        this.nom = nom;
        this.type = type;
        this.contenu = contenu;
        this.taille = contenu.length;
        this.empreinte = ChaineScellee.sha256(contenu);
        this.auteur = auteur;
        this.motif = motif;
        this.creeLe = maintenant;
    }

    public boolean estCourant() {
        return statut != Statut.REMPLACE && statut != Statut.RETIRE;
    }

    public void exigerNonVerrouille() {
        if (statut == Statut.SIGNE) {
            throw new IllegalStateException("Version verrouillée après signature.");
        }
    }

    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public Long getTrousseId() { return trousseId; }
    public String getCleDocument() { return cleDocument; }
    public int getVersion() { return version; }
    public String getNom() { return nom; }
    public FormatDocument.Type getType() { return type; }
    public long getTaille() { return taille; }
    public String getEmpreinte() { return empreinte; }
    public Statut getStatut() { return statut; }
    public void setStatut(Statut statut) { this.statut = statut; }
    public Analyse getAnalyse() { return analyse; }
    public void setAnalyse(Analyse analyse) { this.analyse = analyse; }
    public String getAuteur() { return auteur; }
    public String getMotif() { return motif; }
    public byte[] getContenu() { return contenu; }
    public Instant getCreeLe() { return creeLe; }
}
