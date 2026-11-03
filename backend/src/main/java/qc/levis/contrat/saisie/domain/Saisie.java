package qc.levis.contrat.saisie.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Fiche (soumission) d'un formulaire. Les réponses sont stockées en JSON clés-par-code
 * ({@code donnees_json}) — aucune colonne par champ. {@code parentSaisieId} supporte les fiches
 * liées parent-enfant.
 */
@Entity
@Table(name = "saisie")
public class Saisie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "formulaire_id", nullable = false)
    private Long formulaireId;

    /**
     * Numéro de demande lisible généré côté serveur (ex. {@code AP-2026-000123}), sans donnée
     * personnelle (architecture §5.3). Null tant que la fiche n'a pas encore reçu son numéro.
     */
    @Column(length = 40, unique = true)
    private String numero;

    @Column(name = "version_numero", nullable = false)
    private int versionNumero;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutSaisie statut = StatutSaisie.BROUILLON;

    @Column(name = "donnees_json", length = 1_000_000)
    private String donneesJson;

    @Column(name = "parent_saisie_id")
    private Long parentSaisieId;

    @Column(name = "cree_par", length = 150)
    private String creePar;

    @Column(name = "maj_le", nullable = false)
    private Instant majLe = Instant.now();

    @Column(name = "soumis_le")
    private Instant soumisLe;

    public Saisie() {}

    public Saisie(Long tenantId, Long formulaireId, int versionNumero, String creePar) {
        this.tenantId = tenantId;
        this.formulaireId = formulaireId;
        this.versionNumero = versionNumero;
        this.creePar = creePar;
    }

    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public Long getFormulaireId() { return formulaireId; }
    public void setFormulaireId(Long formulaireId) { this.formulaireId = formulaireId; }
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    public int getVersionNumero() { return versionNumero; }
    public void setVersionNumero(int versionNumero) { this.versionNumero = versionNumero; }
    public StatutSaisie getStatut() { return statut; }
    public void setStatut(StatutSaisie statut) { this.statut = statut; }
    public String getDonneesJson() { return donneesJson; }
    public void setDonneesJson(String donneesJson) { this.donneesJson = donneesJson; }
    public Long getParentSaisieId() { return parentSaisieId; }
    public void setParentSaisieId(Long parentSaisieId) { this.parentSaisieId = parentSaisieId; }
    public String getCreePar() { return creePar; }
    public void setCreePar(String creePar) { this.creePar = creePar; }
    public Instant getMajLe() { return majLe; }
    public void setMajLe(Instant majLe) { this.majLe = majLe; }
    public Instant getSoumisLe() { return soumisLe; }
    public void setSoumisLe(Instant soumisLe) { this.soumisLe = soumisLe; }
}
