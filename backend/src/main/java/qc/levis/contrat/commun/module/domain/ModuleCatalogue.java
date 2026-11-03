package qc.levis.contrat.commun.module.domain;

import jakarta.persistence.*;

/**
 * Catalogue des modules fonctionnels activables/désactivables par tenant depuis l'espace superadmin.
 * Donnée de référence (amorcée) : aucune logique métier en dur ici, seulement la description du module
 * et ses dépendances (codes séparés par des virgules dans {@code requiert}).
 */
@Entity
@Table(name = "module_catalogue")
public class ModuleCatalogue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 200)
    private String nom;

    @Column(length = 50)
    private String famille;

    private int ordre = 0;

    /** État par défaut quand aucun réglage explicite n'existe pour le tenant. */
    @Column(name = "actif_par_defaut", nullable = false)
    private boolean actifParDefaut = false;

    /** Codes des modules prérequis, séparés par des virgules (ex. "formulaire,saisie"). */
    @Column(length = 500)
    private String requiert;

    public ModuleCatalogue() {}

    public ModuleCatalogue(String code, String nom, String famille, int ordre, boolean actifParDefaut, String requiert) {
        this.code = code;
        this.nom = nom;
        this.famille = famille;
        this.ordre = ordre;
        this.actifParDefaut = actifParDefaut;
        this.requiert = requiert;
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getFamille() { return famille; }
    public void setFamille(String famille) { this.famille = famille; }
    public int getOrdre() { return ordre; }
    public void setOrdre(int ordre) { this.ordre = ordre; }
    public boolean isActifParDefaut() { return actifParDefaut; }
    public void setActifParDefaut(boolean actifParDefaut) { this.actifParDefaut = actifParDefaut; }
    public String getRequiert() { return requiert; }
    public void setRequiert(String requiert) { this.requiert = requiert; }
}
