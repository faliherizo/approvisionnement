package qc.levis.contrat.approbation.domain;

import jakarta.persistence.*;
import java.time.Instant;
import qc.levis.contrat.commun.domain.Enums;

/** Étape d'un circuit d'approbation hiérarchisé. */
@Entity
public class ApprovalStep {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long documentId;

    @Column(nullable = false)
    private int niveau;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Enums.Role roleRequis;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Enums.StatutApprobation statut = Enums.StatutApprobation.EN_ATTENTE;

    private String approbateur;
    private String note;
    private Instant decideLe;

    public Long getId() { return id; }
    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long d) { this.documentId = d; }
    public int getNiveau() { return niveau; }
    public void setNiveau(int niveau) { this.niveau = niveau; }
    public Enums.Role getRoleRequis() { return roleRequis; }
    public void setRoleRequis(Enums.Role r) { this.roleRequis = r; }
    public Enums.StatutApprobation getStatut() { return statut; }
    public void setStatut(Enums.StatutApprobation s) { this.statut = s; }
    public String getApprobateur() { return approbateur; }
    public void setApprobateur(String a) { this.approbateur = a; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public Instant getDecideLe() { return decideLe; }
    public void setDecideLe(Instant d) { this.decideLe = d; }
}
