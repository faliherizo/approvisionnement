package qc.levis.contrat.redaction.domain;

import jakarta.persistence.*;

/**
 * Gabarit de document normalisé (appel d'offres public), configurable dynamiquement.
 * {@code tenantId} nul = gabarit du catalogue global (référentiel SCT) ; un gabarit
 * de tenant surcharge le global de même {@code code}. Les champs de design sont
 * facultatifs : nuls, ils retombent sur le branding du tenant (surcouche).
 */
@Entity
@Table(name = "gabarit")
public class Gabarit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id")
    private Long tenantId;

    /** Clé stable du gabarit (ex. « CONTRAT_SERVICES »), unique dans un périmètre tenant. */
    @Column(nullable = false)
    private String code;

    private String nom;

    // --- Présentation (couverture / pied) ---
    @Column(name = "sur_label") private String surLabel;
    private String label;
    @Column(name = "sous_titre") private String sousTitre;
    @Column(name = "pied_label") private String piedLabel;

    /** Mode de rendu du corps : contrat numéroté ou grille de prix. */
    @Enumerated(EnumType.STRING)
    private CorpsGabarit corps = CorpsGabarit.CONTRAT;

    // --- Surcharge de design (facultative : nulle => branding du tenant) ---
    private String police;
    @Column(name = "couleur_titre") private String couleurTitre;
    @Column(name = "couleur_accent") private String couleurAccent;
    // Data URL base64 en TEXT inline (jamais @Lob : l'export lit ce champ hors transaction).
    @Column(name = "logo_image", length = 1_000_000) private String logoImage;
    @Column(name = "afficher_couverture") private Boolean afficherCouverture;

    @Column(name = "marge_gauche") private Integer margeGauche;
    @Column(name = "marge_droite") private Integer margeDroite;
    @Column(name = "marge_haut") private Integer margeHaut;
    @Column(name = "marge_bas") private Integer margeBas;

    private boolean actif = true;
    private Integer ordre;

    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getSurLabel() { return surLabel; }
    public void setSurLabel(String v) { this.surLabel = v; }
    public String getLabel() { return label; }
    public void setLabel(String v) { this.label = v; }
    public String getSousTitre() { return sousTitre; }
    public void setSousTitre(String v) { this.sousTitre = v; }
    public String getPiedLabel() { return piedLabel; }
    public void setPiedLabel(String v) { this.piedLabel = v; }
    public CorpsGabarit getCorps() { return corps; }
    public void setCorps(CorpsGabarit corps) { this.corps = corps; }
    public String getPolice() { return police; }
    public void setPolice(String v) { this.police = v; }
    public String getCouleurTitre() { return couleurTitre; }
    public void setCouleurTitre(String v) { this.couleurTitre = v; }
    public String getCouleurAccent() { return couleurAccent; }
    public void setCouleurAccent(String v) { this.couleurAccent = v; }
    public String getLogoImage() { return logoImage; }
    public void setLogoImage(String v) { this.logoImage = v; }
    public Boolean getAfficherCouverture() { return afficherCouverture; }
    public void setAfficherCouverture(Boolean v) { this.afficherCouverture = v; }
    public Integer getMargeGauche() { return margeGauche; }
    public void setMargeGauche(Integer v) { this.margeGauche = v; }
    public Integer getMargeDroite() { return margeDroite; }
    public void setMargeDroite(Integer v) { this.margeDroite = v; }
    public Integer getMargeHaut() { return margeHaut; }
    public void setMargeHaut(Integer v) { this.margeHaut = v; }
    public Integer getMargeBas() { return margeBas; }
    public void setMargeBas(Integer v) { this.margeBas = v; }
    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }
    public Integer getOrdre() { return ordre; }
    public void setOrdre(Integer ordre) { this.ordre = ordre; }
}
