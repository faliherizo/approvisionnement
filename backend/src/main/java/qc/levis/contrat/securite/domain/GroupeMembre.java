package qc.levis.contrat.securite.domain;

import jakarta.persistence.*;

/** Appartenance d'un utilisateur à un groupe. */
@Entity
@Table(name = "groupe_membre",
        uniqueConstraints = @UniqueConstraint(name = "uq_groupe_membre", columnNames = {"tenant_id", "groupe_code", "utilisateur"}))
public class GroupeMembre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "groupe_code", nullable = false, length = 80)
    private String groupeCode;

    @Column(nullable = false, length = 150)
    private String utilisateur;

    public GroupeMembre() {}

    public GroupeMembre(Long tenantId, String groupeCode, String utilisateur) {
        this.tenantId = tenantId;
        this.groupeCode = groupeCode;
        this.utilisateur = utilisateur;
    }

    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public String getGroupeCode() { return groupeCode; }
    public void setGroupeCode(String groupeCode) { this.groupeCode = groupeCode; }
    public String getUtilisateur() { return utilisateur; }
    public void setUtilisateur(String utilisateur) { this.utilisateur = utilisateur; }
}
