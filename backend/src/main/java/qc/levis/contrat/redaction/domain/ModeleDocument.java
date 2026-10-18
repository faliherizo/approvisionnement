package qc.levis.contrat.redaction.domain;

import jakarta.persistence.*;

/**
 * Modèle (gabarit) de mise en forme des documents générés (PDF/DOCX), propre à
 * chaque entreprise (tenant). Un modèle par tenant ; le modèle de tenant_id nul
 * est le gabarit par défaut de la plateforme.
 */
@Entity
@Table(name = "modele_document")
public class ModeleDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", unique = true)
    private Long tenantId;

    /** Famille de police : HELVETICA, TIMES ou COURIER. */
    private String police = "HELVETICA";

    @Column(name = "couleur_titre") private String couleurTitre;
    @Column(name = "couleur_accent") private String couleurAccent;
    private String marque;

    @Column(name = "pied_de_page") private String piedDePage;

    @Column(name = "afficher_couverture") private boolean afficherCouverture = true;

    // Data URL base64 stockée en TEXT inline (pas @Lob : évite les Large Objects PostgreSQL
    // qui exigent une transaction — l'export lit ce champ hors transaction).
    @Column(name = "logo_image", length = 1_000_000) private String logoImage;

    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public String getPolice() { return police; }
    public void setPolice(String police) { this.police = police; }
    public String getCouleurTitre() { return couleurTitre; }
    public void setCouleurTitre(String v) { this.couleurTitre = v; }
    public String getCouleurAccent() { return couleurAccent; }
    public void setCouleurAccent(String v) { this.couleurAccent = v; }
    public String getMarque() { return marque; }
    public void setMarque(String marque) { this.marque = marque; }
    public String getPiedDePage() { return piedDePage; }
    public void setPiedDePage(String v) { this.piedDePage = v; }
    public boolean isAfficherCouverture() { return afficherCouverture; }
    public void setAfficherCouverture(boolean v) { this.afficherCouverture = v; }
    public String getLogoImage() { return logoImage; }
    public void setLogoImage(String v) { this.logoImage = v; }
}
