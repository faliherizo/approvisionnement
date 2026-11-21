package qc.levis.contrat.rapport.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Locale;

/**
 * Tableau de bord configurable (CHU 6.04) : un nom, ses indicateurs (décrits en JSON) et son propriétaire, seul à le
 * modifier. Partagé, il est visible de toute l'organisation.
 */
@Entity
@Table(name = "rapport_tableau_bord")
public class TableauBord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, updatable = false) private Long tenantId;
    @Column(nullable = false, length = 120) private String nom;
    @Column(nullable = false, updatable = false, length = 150) private String proprietaire;
    @Column(nullable = false) private boolean partage;
    @Column(nullable = false, length = 20000) private String indicateursJson;
    @Column(nullable = false) private Instant modifieLe;

    protected TableauBord() { }

    public TableauBord(Long tenantId, String proprietaire, Instant le) {
        this.tenantId = tenantId;
        this.proprietaire = proprietaire.toLowerCase(Locale.ROOT);
        this.modifieLe = le;
        this.indicateursJson = "[]";
    }

    public void configurer(String nom, boolean partage, String indicateursJson, Instant le) {
        if (nom == null || nom.isBlank() || nom.length() > 120) throw new IllegalArgumentException("Nom du tableau de bord requis (120 caractères au plus).");
        this.nom = nom.strip();
        this.partage = partage;
        this.indicateursJson = indicateursJson;
        this.modifieLe = le;
    }

    public boolean estProprietaire(String courriel) {
        return courriel != null && proprietaire.equals(courriel.toLowerCase(Locale.ROOT));
    }

    public boolean estVisiblePar(String courriel) {
        return partage || estProprietaire(courriel);
    }

    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public String getNom() { return nom; }
    public String getProprietaire() { return proprietaire; }
    public boolean isPartage() { return partage; }
    public String getIndicateursJson() { return indicateursJson; }
    public Instant getModifieLe() { return modifieLe; }
}
