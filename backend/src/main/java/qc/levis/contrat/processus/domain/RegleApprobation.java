package qc.levis.contrat.processus.domain;

import jakarta.persistence.*;

/**
 * Règle d'approbation configurable (moteur multi-niveaux {@link Approbations}) : « pour {@code documentType},
 * si {@code champ operateur valeur}, exiger une approbation de niveau {@code niveau} par {@code role/id} ».
 */
@Entity
@Table(name = "regle_approbation", indexes = @Index(name = "ix_regleappro", columnList = "tenant_id,document_type"))
public class RegleApprobation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "document_type", nullable = false, length = 80)
    private String documentType;

    @Column(nullable = false, length = 80)
    private String champ;

    @Column(nullable = false, length = 10)
    private String operateur; // gte | lte | eq | neq

    @Column(length = 200)
    private String valeur;

    @Column(nullable = false)
    private int niveau;

    @Column(name = "approbateur_role", length = 60)
    private String approbateurRole;

    @Column(name = "approbateur_id", length = 150)
    private String approbateurId;

    public RegleApprobation() {}

    public RegleApprobation(Long tenantId, String documentType, String champ, String operateur, String valeur,
                            int niveau, String approbateurRole, String approbateurId) {
        this.tenantId = tenantId;
        this.documentType = documentType;
        this.champ = champ;
        this.operateur = operateur;
        this.valeur = valeur;
        this.niveau = niveau;
        this.approbateurRole = approbateurRole;
        this.approbateurId = approbateurId;
    }

    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public String getDocumentType() { return documentType; }
    public String getChamp() { return champ; }
    public String getOperateur() { return operateur; }
    public String getValeur() { return valeur; }
    public int getNiveau() { return niveau; }
    public String getApprobateurRole() { return approbateurRole; }
    public String getApprobateurId() { return approbateurId; }
}
