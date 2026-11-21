package qc.levis.contrat.audit.domain;

import jakarta.persistence.*;
import java.time.Instant;
import qc.levis.contrat.tenant.domain.Tenant;

/** Journal d'audit transversal (traçabilité - Loi 25). */
@Entity
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String utilisateur;
    private String action;
    private String entite;
    private Long entiteId;

    @Column(length = 2000)
    private String details;

    private Instant horodatage = Instant.now();

    /** Tenant propriétaire de l'événement (isolation multi-tenant) ; nul = action globale/superadmin. */
    @Column(name = "tenant_id")
    private Long tenantId;

    public AuditLog() {}

    public AuditLog(String utilisateur, String action, String entite, Long entiteId, String details) {
        this.utilisateur = utilisateur;
        this.action = action;
        this.entite = entite;
        this.entiteId = entiteId;
        this.details = details;
    }

    public Long getId() { return id; }
    public String getUtilisateur() { return utilisateur; }
    public String getAction() { return action; }
    public String getEntite() { return entite; }
    public Long getEntiteId() { return entiteId; }
    public String getDetails() { return details; }
    public Instant getHorodatage() { return horodatage; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }

    /** Loi 25 : l'événement reste au journal, mais la personne n'y est plus nommée (auteur et mentions dans le détail). */
    public void pseudonymiser(String courriel, String pseudonyme) {
        if (courriel.equalsIgnoreCase(utilisateur)) utilisateur = pseudonyme;
        if (details != null) details = details.replaceAll("(?i)" + java.util.regex.Pattern.quote(courriel), pseudonyme);
    }
}
