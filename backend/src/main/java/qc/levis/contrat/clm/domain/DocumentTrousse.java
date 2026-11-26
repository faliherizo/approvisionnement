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
import jakarta.persistence.Transient;

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

    // « analyse » est un mot réservé de PostgreSQL : la colonne s'appelle statut_analyse (migration 074).
    @Enumerated(EnumType.STRING)
    @Column(name = "statut_analyse", nullable = false, length = 12)
    private Analyse analyse = Analyse.EN_ATTENTE;

    @Column(nullable = false, length = 150)
    private String auteur;

    @Column(length = 500)
    private String motif;

    /** Forme stockée : enveloppe chiffrée par la clé de l'organisation (ADR-037), ou ancien contenu en clair. */
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "contenu", nullable = false)
    private byte[] contenu;

    /** Version de la clé de données qui a chiffré le contenu ; nulle pour un ancien contenu en clair (migration 081). */
    @Column(name = "cle_version")
    private Integer cleVersion;

    /** Étiquettes Microsoft Purview apposées au document (ADR-036), conservées avec lui. */
    @Column(name = "etiquette_sensibilite_id", length = EtiquettePurview.ID_MAX)
    private String etiquetteSensibiliteId;

    @Column(name = "etiquette_sensibilite", length = EtiquettePurview.NOM_MAX)
    private String etiquetteSensibilite;

    @Column(name = "etiquette_retention", length = EtiquettePurview.NOM_MAX)
    private String etiquetteRetention;

    @Column(name = "etiquette_retention_jours")
    private Integer etiquetteRetentionJours;

    /** Contenu en clair, en mémoire seulement : déchiffré au chargement, jamais écrit en base. */
    @Transient
    private byte[] clair;

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
    /** Contenu en clair : celui que l'on a déposé, ou celui déchiffré au chargement. */
    public byte[] getContenu() { return clair != null ? clair : contenu; }

    /** Forme stockée en base (enveloppe chiffrée ou ancien clair), pour le chiffrement au repos. */
    public byte[] getStockage() { return contenu; }

    public Integer getCleVersion() { return cleVersion; }

    /**
     * Chiffrement au repos (ADR-037) : la forme stockée devient l'enveloppe ; le clair reste en mémoire pour la suite de la
     * transaction. L'empreinte et la taille, calculées sur le clair, ne changent pas.
     */
    public void stockerChiffre(byte[] enveloppe, int version) {
        if (clair == null) {
            clair = contenu;
        }
        contenu = enveloppe;
        cleVersion = version;
    }

    /** Clair obtenu au chargement par le déchiffrement de la forme stockée. */
    public void dechiffre(byte[] contenuClair) {
        clair = contenuClair;
    }

    /** Étiquettes Purview de la politique applicable (ADR-036) ; nulles : l'étiquette correspondante est retirée. */
    public void etiqueter(EtiquettePurview sensibilite, EtiquettePurview retention) {
        etiquetteSensibiliteId = sensibilite == null ? null : sensibilite.id();
        etiquetteSensibilite = sensibilite == null ? null : sensibilite.nom();
        etiquetteRetention = retention == null ? null : retention.nom();
        etiquetteRetentionJours = retention == null ? null : retention.retentionJours();
    }

    /** Une version produite (version signée) garde les étiquettes de sa source. */
    public void reprendreEtiquettes(DocumentTrousse source) {
        etiqueter(source.sensibilite(), source.retention());
    }

    public EtiquettePurview sensibilite() {
        return etiquetteSensibiliteId == null ? null : EtiquettePurview.sensibilite(etiquetteSensibiliteId, etiquetteSensibilite);
    }

    public EtiquettePurview retention() {
        return etiquetteRetention == null ? null : EtiquettePurview.retention(etiquetteRetention, etiquetteRetentionJours);
    }

    /** Destruction à l'échéance de conservation : le contenu disparaît, l'empreinte demeure comme preuve de ce qui fut. */
    public void effacerContenu() {
        this.contenu = new byte[0];
        this.clair = null;
    }
    public Instant getCreeLe() { return creeLe; }
}
