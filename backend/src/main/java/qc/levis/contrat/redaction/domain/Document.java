package qc.levis.contrat.redaction.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import qc.levis.contrat.commun.domain.Enums;
import qc.levis.contrat.dossier.domain.Dossier;

/** Document contractuel (devis, contrat de gré à gré, addenda). */
@Entity
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Enums.TypeContrat type;

    @Enumerated(EnumType.STRING)
    private Enums.ModeSollicitation modeSollicitation;

    private BigDecimal valeurEstimee;
    private String contexte;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Enums.StatutDocument statut = Enums.StatutDocument.BROUILLON;

    private String creePar;
    private Instant creeLe = Instant.now();

    /** Suppression logique : un document supprimé reste en base mais est masqué des listes. */
    @Column(nullable = false)
    private boolean actif = true;

    // --- Métadonnées de contrat formel ---
    @Column(name = "numero_ao")
    private String numeroAO;
    @Column(name = "organisme_public", length = 1000)
    private String organismePublic;
    @Column(length = 1000)
    private String prestataire;

    /** Gabarit de rendu par défaut (issu du template), code de {@code Gabarit} ; nul = déduit du type. */
    @Column(name = "gabarit_code")
    private String gabaritCode;

    @Column(name = "tenant_id")
    private Long tenantId;

    /** Dossier d'approvisionnement de rattachement (art. 15) ; nul = document isolé. */
    @Column(name = "dossier_id")
    private Long dossierId;

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("ordre ASC")
    private List<DocumentClause> clauses = new ArrayList<>();

    public Long getId() { return id; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public Enums.TypeContrat getType() { return type; }
    public void setType(Enums.TypeContrat type) { this.type = type; }
    public Enums.ModeSollicitation getModeSollicitation() { return modeSollicitation; }
    public void setModeSollicitation(Enums.ModeSollicitation m) { this.modeSollicitation = m; }
    public BigDecimal getValeurEstimee() { return valeurEstimee; }
    public void setValeurEstimee(BigDecimal v) { this.valeurEstimee = v; }
    public String getContexte() { return contexte; }
    public void setContexte(String contexte) { this.contexte = contexte; }
    public Enums.StatutDocument getStatut() { return statut; }
    public void setStatut(Enums.StatutDocument statut) { this.statut = statut; }
    public String getCreePar() { return creePar; }
    public void setCreePar(String creePar) { this.creePar = creePar; }
    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public Long getDossierId() { return dossierId; }
    public void setDossierId(Long dossierId) { this.dossierId = dossierId; }
    public String getNumeroAO() { return numeroAO; }
    public void setNumeroAO(String numeroAO) { this.numeroAO = numeroAO; }
    public String getOrganismePublic() { return organismePublic; }
    public void setOrganismePublic(String organismePublic) { this.organismePublic = organismePublic; }
    public String getPrestataire() { return prestataire; }
    public void setPrestataire(String prestataire) { this.prestataire = prestataire; }
    public String getGabaritCode() { return gabaritCode; }
    public void setGabaritCode(String gabaritCode) { this.gabaritCode = gabaritCode; }
    public Instant getCreeLe() { return creeLe; }
    public void setCreeLe(Instant creeLe) { this.creeLe = creeLe; }
    public List<DocumentClause> getClauses() { return clauses; }
    public void setClauses(List<DocumentClause> clauses) { this.clauses = clauses; }
}
