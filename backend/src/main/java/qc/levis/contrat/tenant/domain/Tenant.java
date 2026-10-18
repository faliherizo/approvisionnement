package qc.levis.contrat.tenant.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Locataire (tenant) : espace isolé d'un organisme. Registre géré par le superadmin ;
 * cette entité n'est PAS elle-même scopée par tenant.
 */
@Entity
public class Tenant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Identifiant court (sous-domaine / sélecteur de connexion). */
    @Column(nullable = false, unique = true)
    private String slug;

    /** Titre affiché sur la page de connexion et l'en-tête de l'espace. */
    @Column(nullable = false)
    private String titre;

    private String sousTitre;

    @Column(nullable = false)
    private boolean actif = true;

    // --- Thème / design (personnalisable par le superadmin) ---
    @Column(name = "couleur_primaire") private String couleurPrimaire;
    @Column(name = "couleur_accent") private String couleurAccent;
    @Column(name = "couleur_fond") private String couleurFond;
    @Column(name = "couleur_texte") private String couleurTexte;
    @Column(name = "logo_texte") private String logoTexte;

    /** Logo image (data URL base64). Stocké en TEXT inline (pas @Lob : évite les Large
     *  Objects PostgreSQL qui exigeraient une transaction lors de la lecture). */
    @Column(name = "logo_image", length = 1_000_000) private String logoImage;

    private Instant creeLe = Instant.now();

    /** Assistant IA activé pour ce tenant (art. 21 : option de désactivation). Activé par défaut. */
    @Column(name = "ia_activee", nullable = false)
    private boolean iaActivee = true;

    public Tenant() {}

    public Tenant(String slug, String titre, String sousTitre) {
        this.slug = slug;
        this.titre = titre;
        this.sousTitre = sousTitre;
    }

    public Long getId() { return id; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getSousTitre() { return sousTitre; }
    public void setSousTitre(String sousTitre) { this.sousTitre = sousTitre; }
    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }
    public boolean isIaActivee() { return iaActivee; }
    public void setIaActivee(boolean iaActivee) { this.iaActivee = iaActivee; }
    public String getCouleurPrimaire() { return couleurPrimaire; }
    public void setCouleurPrimaire(String v) { this.couleurPrimaire = v; }
    public String getCouleurAccent() { return couleurAccent; }
    public void setCouleurAccent(String v) { this.couleurAccent = v; }
    public String getCouleurFond() { return couleurFond; }
    public void setCouleurFond(String v) { this.couleurFond = v; }
    public String getCouleurTexte() { return couleurTexte; }
    public void setCouleurTexte(String v) { this.couleurTexte = v; }
    public String getLogoTexte() { return logoTexte; }
    public void setLogoTexte(String v) { this.logoTexte = v; }
    public String getLogoImage() { return logoImage; }
    public void setLogoImage(String v) { this.logoImage = v; }
    public Instant getCreeLe() { return creeLe; }
    public void setCreeLe(Instant creeLe) { this.creeLe = creeLe; }
}
