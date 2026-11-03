package qc.levis.contrat.catalogue.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Application / espace fonctionnel (écran 2) : regroupe des formulaires d'un domaine pour un environnement
 * donné, avec propriétaire et état de publication. Entité du contexte « catalogue » (le module s'appelle
 * {@code application}). Aucun contenu figé : les formulaires sont rattachés par domaine.
 */
@Entity
@Table(name = "application_catalogue",
        uniqueConstraints = @UniqueConstraint(name = "uq_application_tenant_code", columnNames = {"tenant_id", "code"}))
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(nullable = false, length = 80)
    private String code;

    @Column(nullable = false, length = 200)
    private String nom;

    @Column(length = 40)
    private String domaine;

    @Column(length = 20)
    private String environnement = "DEV"; // DEV | UAT | PROD

    @Column(length = 20)
    private String statut = "BROUILLON"; // BROUILLON | TEST | ACTIF

    @Column(length = 200)
    private String proprietaire;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private boolean actif = true;

    @Column(name = "cree_le", nullable = false)
    private Instant creeLe = Instant.now();

    public Application() {}

    public Application(Long tenantId, String code, String nom, String domaine, String environnement,
                       String statut, String proprietaire, String description) {
        this.tenantId = tenantId;
        this.code = code;
        this.nom = nom;
        this.domaine = domaine;
        this.environnement = environnement;
        this.statut = statut;
        this.proprietaire = proprietaire;
        this.description = description;
    }

    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getDomaine() { return domaine; }
    public void setDomaine(String domaine) { this.domaine = domaine; }
    public String getEnvironnement() { return environnement; }
    public void setEnvironnement(String environnement) { this.environnement = environnement; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public String getProprietaire() { return proprietaire; }
    public void setProprietaire(String proprietaire) { this.proprietaire = proprietaire; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }
    public Instant getCreeLe() { return creeLe; }
}
