package qc.levis.contrat.dossier.domain;

import jakarta.persistence.*;
import java.time.Instant;
import qc.levis.contrat.commun.domain.Enums;
import qc.levis.contrat.redaction.domain.Document;

/**
 * Dossier d'approvisionnement : regroupe plusieurs documents d'un même processus
 * d'acquisition (appel d'offres, demande de prix, contrat…) — exigence art. 15 de
 * l'appel d'offres n° 7000017905. Agrégat de <em>regroupement</em> : il ne duplique pas
 * le {@link Document}, qui lui reste rattaché par {@code Document.dossierId}.
 */
@Entity
@Table(name = "dossier")
public class Dossier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titre;

    @Column(name = "numero_ao")
    private String numeroAO;

    @Column(length = 2000)
    private String objet;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_acquisition")
    private Enums.TypeAcquisition typeAcquisition;

    @Enumerated(EnumType.STRING)
    private Enums.ModeSollicitation modeSollicitation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Enums.StatutDossier statut = Enums.StatutDossier.OUVERT;

    private String responsable;
    private String creePar;
    private Instant creeLe = Instant.now();

    @Column(nullable = false)
    private boolean actif = true;

    @Column(name = "tenant_id")
    private Long tenantId;

    /**
     * Fait avancer le statut vers {@code cible} en refusant les transitions depuis un
     * dossier déjà terminal (clôturé/annulé) — invariant du cycle de vie.
     */
    public void changerStatut(Enums.StatutDossier cible) {
        if (statut == Enums.StatutDossier.CLOTURE || statut == Enums.StatutDossier.ANNULE) {
            throw new IllegalStateException("Dossier terminé : statut non modifiable.");
        }
        this.statut = cible;
    }

    public Long getId() { return id; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getNumeroAO() { return numeroAO; }
    public void setNumeroAO(String numeroAO) { this.numeroAO = numeroAO; }
    public String getObjet() { return objet; }
    public void setObjet(String objet) { this.objet = objet; }
    public Enums.TypeAcquisition getTypeAcquisition() { return typeAcquisition; }
    public void setTypeAcquisition(Enums.TypeAcquisition t) { this.typeAcquisition = t; }
    public Enums.ModeSollicitation getModeSollicitation() { return modeSollicitation; }
    public void setModeSollicitation(Enums.ModeSollicitation m) { this.modeSollicitation = m; }
    public Enums.StatutDossier getStatut() { return statut; }
    public void setStatut(Enums.StatutDossier statut) { this.statut = statut; }
    public String getResponsable() { return responsable; }
    public void setResponsable(String responsable) { this.responsable = responsable; }
    public String getCreePar() { return creePar; }
    public void setCreePar(String creePar) { this.creePar = creePar; }
    public Instant getCreeLe() { return creeLe; }
    public void setCreeLe(Instant creeLe) { this.creeLe = creeLe; }
    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
}
