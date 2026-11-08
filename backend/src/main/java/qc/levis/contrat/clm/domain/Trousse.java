package qc.levis.contrat.clm.domain;

import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

/**
 * Trousse (ST-03) : l'unité de travail qui regroupe documents, participants, paramètres de parcours et état, et
 * qui garde la corrélation avec le dossier source Salesforce.
 */
@Entity
@Table(name = "clm_trousse", uniqueConstraints = @UniqueConstraint(name = "uk_clm_trousse_ref", columnNames = {"tenant_id", "reference"}))
public class Trousse {

    public enum Source { MANUELLE, SALESFORCE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(nullable = false, length = 60)
    private String reference;

    @Column(nullable = false, length = 250)
    private String titre;

    @Column(name = "type_contrat", length = 80)
    private String typeContrat;

    @Column(length = 120)
    private String unite;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Source source = Source.MANUELLE;

    @Column(name = "sf_dossier_id", length = 80)
    private String sfDossierId;

    @Column(name = "sf_recupere_le")
    private Instant sfRecupereLe;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutTrousse statut = StatutTrousse.BROUILLON;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Identite.Risque risque = Identite.Risque.MOYEN;

    @Column(nullable = false, length = 150)
    private String proprietaire;

    private LocalDate echeance;

    @Column(name = "modele_id")
    private Long modeleId;

    @Column(name = "valeurs_json", length = 100_000)
    private String valeursJson = "{}";

    @Column(name = "rappel_frequence_jours", nullable = false)
    private int rappelFrequenceJours = Relances.FREQUENCE_PAR_DEFAUT_JOURS;

    @Column(name = "rappels_max", nullable = false)
    private int rappelsMax = Relances.RAPPELS_PAR_DEFAUT;

    @Column(length = 1000)
    private String copies;

    @Column(name = "cree_le", nullable = false)
    private Instant creeLe;

    @Column(name = "maj_le", nullable = false)
    private Instant majLe;

    @Version
    private long version;

    protected Trousse() {
    }

    public Trousse(Long tenantId, String reference, String titre, String proprietaire, Instant maintenant) {
        this.tenantId = tenantId;
        this.reference = reference;
        this.titre = titre;
        this.proprietaire = proprietaire;
        this.creeLe = maintenant;
        this.majLe = maintenant;
    }

    public void changerStatut(StatutTrousse cible, Instant maintenant) {
        this.statut = statut.vers(cible);
        this.majLe = maintenant;
    }

    public void exigerModifiable() {
        if (!statut.estModifiable()) {
            throw new IllegalStateException("La trousse n'est plus modifiable (statut " + statut + ").");
        }
    }

    public void toucher(Instant maintenant) {
        this.majLe = maintenant;
    }

    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public String getReference() { return reference; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getTypeContrat() { return typeContrat; }
    public void setTypeContrat(String typeContrat) { this.typeContrat = typeContrat; }
    public String getUnite() { return unite; }
    public void setUnite(String unite) { this.unite = unite; }
    public Source getSource() { return source; }
    public void setSource(Source source) { this.source = source; }
    public String getSfDossierId() { return sfDossierId; }
    public void setSfDossierId(String sfDossierId) { this.sfDossierId = sfDossierId; }
    public Instant getSfRecupereLe() { return sfRecupereLe; }
    public void setSfRecupereLe(Instant sfRecupereLe) { this.sfRecupereLe = sfRecupereLe; }
    public StatutTrousse getStatut() { return statut; }
    public Identite.Risque getRisque() { return risque; }
    public void setRisque(Identite.Risque risque) { this.risque = risque; }
    public String getProprietaire() { return proprietaire; }
    public LocalDate getEcheance() { return echeance; }
    public void setEcheance(LocalDate echeance) { this.echeance = echeance; }
    public Long getModeleId() { return modeleId; }
    public void setModeleId(Long modeleId) { this.modeleId = modeleId; }
    public String getValeursJson() { return valeursJson; }
    public void setValeursJson(String valeursJson) { this.valeursJson = valeursJson; }
    public int getRappelFrequenceJours() { return rappelFrequenceJours; }
    public void setRappelFrequenceJours(int v) { this.rappelFrequenceJours = v; }
    public int getRappelsMax() { return rappelsMax; }
    public void setRappelsMax(int v) { this.rappelsMax = v; }
    public String getCopies() { return copies; }
    public void setCopies(String copies) { this.copies = copies; }
    public Instant getCreeLe() { return creeLe; }
    public Instant getMajLe() { return majLe; }
}
