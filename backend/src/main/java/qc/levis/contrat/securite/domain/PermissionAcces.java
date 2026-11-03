package qc.levis.contrat.securite.domain;

import jakarta.persistence.*;

/** Permission d'accès granulaire persistée (tenant-scopée). Convertie en {@link RegleAcces} pour l'évaluation. */
@Entity
@Table(name = "permission_acces")
public class PermissionAcces {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "sujet_type", nullable = false, length = 20)
    private String sujetType;

    @Column(nullable = false, length = 150)
    private String sujet;

    @Column(nullable = false, length = 20)
    private String portee;

    @Column(nullable = false, length = 120)
    private String cible;

    @Column(nullable = false, length = 20)
    private String action;

    public PermissionAcces() {}

    public PermissionAcces(Long tenantId, String sujetType, String sujet, String portee, String cible, String action) {
        this.tenantId = tenantId;
        this.sujetType = sujetType;
        this.sujet = sujet;
        this.portee = portee;
        this.cible = cible;
        this.action = action;
    }

    public RegleAcces versRegle() { return new RegleAcces(sujetType, sujet, portee, cible, action); }

    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public String getSujetType() { return sujetType; }
    public void setSujetType(String sujetType) { this.sujetType = sujetType; }
    public String getSujet() { return sujet; }
    public void setSujet(String sujet) { this.sujet = sujet; }
    public String getPortee() { return portee; }
    public void setPortee(String portee) { this.portee = portee; }
    public String getCible() { return cible; }
    public void setCible(String cible) { this.cible = cible; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
}
