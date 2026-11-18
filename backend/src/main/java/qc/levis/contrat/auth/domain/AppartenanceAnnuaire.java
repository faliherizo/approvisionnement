package qc.levis.contrat.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/**
 * Fait d'annuaire conservé pour un compte, selon son origine : la connexion unique (SSO) rapporte groupes et attributs à
 * chaque connexion ; SCIM (Saviynt) pousse les appartenances au fil de l'eau. Seuls les attributs cités par une
 * correspondance sont conservés (minimisation).
 */
@Entity
@Table(name = "annuaire_appartenance", indexes = @Index(name = "ix_annuaire_appartenance_compte", columnList = "tenant_id,courriel"))
public class AppartenanceAnnuaire {

    public enum Origine { SSO, SCIM }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(nullable = false, length = 200)
    private String courriel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 6)
    private Origine origine;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private FaitAnnuaire.Source source;

    @Column(length = 200)
    private String attribut;

    @Column(nullable = false, length = 255)
    private String valeur;

    protected AppartenanceAnnuaire() {
    }

    public AppartenanceAnnuaire(Long tenantId, String courriel, Origine origine, FaitAnnuaire fait) {
        this.tenantId = tenantId;
        this.courriel = courriel;
        this.origine = origine;
        this.source = fait.source();
        this.attribut = fait.attribut();
        this.valeur = fait.valeur().length() > 255 ? fait.valeur().substring(0, 255) : fait.valeur();
    }

    public FaitAnnuaire fait() {
        return new FaitAnnuaire(source, attribut, valeur);
    }

    public Long getId() { return id; }
    public String getCourriel() { return courriel; }
    public Origine getOrigine() { return origine; }
}
