package qc.levis.contrat.document.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Pièce jointe d'une demande. Les métadonnées sont en base ; le binaire est stocké en {@code bytea}
 * (colonne {@code contenu}, PAS de {@code @Lob}/OID — cf. contrainte de portabilité PostgreSQL/H2).
 * En production, le binaire irait dans un stockage objet privé (ADR-005) ; la clé remplacerait {@code contenu}.
 */
@Entity
@Table(name = "document_demande", indexes = @Index(name = "ix_docdem_saisie", columnList = "tenant_id,saisie_id"))
public class DocumentDemande {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "saisie_id", nullable = false)
    private Long saisieId;

    @Column(name = "formulaire_id", nullable = false)
    private Long formulaireId;

    @Column(name = "nom_original", nullable = false, length = 300)
    private String nomOriginal;

    @Column(name = "media_type", length = 150)
    private String mediaType;

    @Column(name = "taille_octets")
    private long tailleOctets;

    @Column(length = 20)
    private String classification = "interne";

    @Column(name = "contenu")
    private byte[] contenu;

    @Column(name = "uploade_par", length = 150)
    private String uploadePar;

    @Column(name = "uploade_le", nullable = false)
    private Instant uploadeLe = Instant.now();

    public DocumentDemande() {}

    public DocumentDemande(Long tenantId, Long saisieId, Long formulaireId, String nomOriginal, String mediaType,
                           long tailleOctets, String classification, byte[] contenu, String uploadePar) {
        this.tenantId = tenantId;
        this.saisieId = saisieId;
        this.formulaireId = formulaireId;
        this.nomOriginal = nomOriginal;
        this.mediaType = mediaType;
        this.tailleOctets = tailleOctets;
        this.classification = classification;
        this.contenu = contenu;
        this.uploadePar = uploadePar;
    }

    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public Long getSaisieId() { return saisieId; }
    public Long getFormulaireId() { return formulaireId; }
    public String getNomOriginal() { return nomOriginal; }
    public String getMediaType() { return mediaType; }
    public long getTailleOctets() { return tailleOctets; }
    public String getClassification() { return classification; }
    public byte[] getContenu() { return contenu; }
    public String getUploadePar() { return uploadePar; }
    public Instant getUploadeLe() { return uploadeLe; }
}
