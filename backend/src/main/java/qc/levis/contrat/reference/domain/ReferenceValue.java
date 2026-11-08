package qc.levis.contrat.reference.domain;

import jakarta.persistence.*;

/**
 * Valeur de référence configurable alimentant les sélecteurs du frontend
 * (types de contrat, modes de sollicitation, catégories…). {@code tenantId} null =
 * valeur globale (gérée par le superadmin) ; sinon spécifique au tenant (gérée par l'admin).
 */
@Entity
@Table(name = "reference_value")
public class ReferenceValue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String categorie;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String libelle;

    @Column(name = "tenant_id")
    private Long tenantId;

    private int ordre = 0;

    @Column(nullable = false)
    private boolean actif = true;

    /** Complément propre à la liste (ex. mots-clés de classification), selon sa déclaration. */
    @Column(length = 2000)
    private String detail;

    public ReferenceValue() {}

    public ReferenceValue(String categorie, String code, String libelle, Long tenantId, int ordre) {
        this.categorie = categorie;
        this.code = code;
        this.libelle = libelle;
        this.tenantId = tenantId;
        this.ordre = ordre;
    }

    public Long getId() { return id; }
    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public int getOrdre() { return ordre; }
    public void setOrdre(int ordre) { this.ordre = ordre; }
    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
}
