package qc.levis.contrat.redaction.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import qc.levis.contrat.commun.domain.Enums;

/** Obligation / échéance contractuelle (post-exécution : livrables, renouvellements…). */
@Entity
public class Obligation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long documentId;

    @Column(nullable = false)
    private String titre;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Enums.ObligationType type = Enums.ObligationType.AUTRE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Enums.ObligationStatut statut = Enums.ObligationStatut.A_VENIR;

    private LocalDate dateEcheance;
    private String responsable;

    @Column(name = "tenant_id")
    private Long tenantId;

    public Long getId() { return id; }
    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Enums.ObligationType getType() { return type; }
    public void setType(Enums.ObligationType type) { this.type = type; }
    public Enums.ObligationStatut getStatut() { return statut; }
    public void setStatut(Enums.ObligationStatut statut) { this.statut = statut; }
    public LocalDate getDateEcheance() { return dateEcheance; }
    public void setDateEcheance(LocalDate d) { this.dateEcheance = d; }
    public String getResponsable() { return responsable; }
    public void setResponsable(String responsable) { this.responsable = responsable; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
}
