package qc.levis.contrat.securite.domain;

import jakarta.persistence.*;

/** Groupe d'utilisateurs (tenant-scopé), porteur de droits via les permissions ciblant GROUPE:code. */
@Entity
@Table(name = "groupe_acces",
        uniqueConstraints = @UniqueConstraint(name = "uq_groupe_code", columnNames = {"tenant_id", "code"}))
public class Groupe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(nullable = false, length = 80)
    private String code;

    @Column(nullable = false, length = 200)
    private String nom;

    public Groupe() {}

    public Groupe(Long tenantId, String code, String nom) {
        this.tenantId = tenantId;
        this.code = code;
        this.nom = nom;
    }

    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
}
