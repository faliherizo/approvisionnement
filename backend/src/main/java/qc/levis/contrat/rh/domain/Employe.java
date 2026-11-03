package qc.levis.contrat.rh.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Dossier employé (RH). Inspiré du modèle Nexu VINA (`paie.Personnel.Employe`). Le NAS n'est jamais
 * relu en clair (masqué dans la vue — Loi 25). {@code attributsJson} permet des champs additionnels
 * configurables sans migration (principe « rien de figé »).
 */
@Entity
@Table(name = "employe",
        uniqueConstraints = @UniqueConstraint(name = "uq_employe_tenant_matricule", columnNames = {"tenant_id", "matricule"}))
public class Employe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(length = 40)
    private String matricule;

    @Column(nullable = false, length = 120)
    private String prenom;

    @Column(nullable = false, length = 120)
    private String nom;

    @Column(length = 15)
    private String nas;

    @Column(length = 180)
    private String courriel;

    @Column(length = 40)
    private String telephone;

    @Column(name = "date_embauche", length = 10)
    private String dateEmbauche;

    @Column(name = "date_fin", length = 10)
    private String dateFin;

    @Column(length = 20)
    private String statut = "ACTIF"; // ACTIF | INACTIF

    @Column(length = 150)
    private String poste;

    @Column(length = 120)
    private String departement;

    @Column(name = "salaire_annuel")
    private double salaireAnnuel;

    @Column(length = 20)
    private String frequence = "biweekly";

    @Column(length = 40)
    private String province = "QC";

    @Column(name = "type_travailleur", length = 40)
    private String typeTravailleur = "employee";

    @Column(name = "attributs_json", length = 100_000)
    private String attributsJson;

    @Column(nullable = false)
    private boolean actif = true;

    @Column(name = "cree_le", nullable = false)
    private Instant creeLe = Instant.now();

    public Employe() {}

    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public String getMatricule() { return matricule; }
    public void setMatricule(String matricule) { this.matricule = matricule; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getNas() { return nas; }
    public void setNas(String nas) { this.nas = nas; }
    public String getCourriel() { return courriel; }
    public void setCourriel(String courriel) { this.courriel = courriel; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public String getDateEmbauche() { return dateEmbauche; }
    public void setDateEmbauche(String dateEmbauche) { this.dateEmbauche = dateEmbauche; }
    public String getDateFin() { return dateFin; }
    public void setDateFin(String dateFin) { this.dateFin = dateFin; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public String getPoste() { return poste; }
    public void setPoste(String poste) { this.poste = poste; }
    public String getDepartement() { return departement; }
    public void setDepartement(String departement) { this.departement = departement; }
    public double getSalaireAnnuel() { return salaireAnnuel; }
    public void setSalaireAnnuel(double salaireAnnuel) { this.salaireAnnuel = salaireAnnuel; }
    public String getFrequence() { return frequence; }
    public void setFrequence(String frequence) { this.frequence = frequence; }
    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }
    public String getTypeTravailleur() { return typeTravailleur; }
    public void setTypeTravailleur(String typeTravailleur) { this.typeTravailleur = typeTravailleur; }
    public String getAttributsJson() { return attributsJson; }
    public void setAttributsJson(String attributsJson) { this.attributsJson = attributsJson; }
    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }
    public Instant getCreeLe() { return creeLe; }
}
