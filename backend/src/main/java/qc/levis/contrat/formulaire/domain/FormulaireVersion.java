package qc.levis.contrat.formulaire.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Version d'un formulaire. {@code structureJson} contient TOUTE la structure sous forme de données
 * (pages, champs, types référencés par code, options, validations, règles de visibilité/calcul) —
 * aucun schéma figé. Le versionnage permet l'approbation avant publication et le retour arrière.
 */
@Entity
@Table(name = "formulaire_version",
        uniqueConstraints = @UniqueConstraint(name = "uq_formulaire_version", columnNames = {"formulaire_id", "numero"}))
public class FormulaireVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "formulaire_id", nullable = false)
    private Long formulaireId;

    @Column(nullable = false)
    private int numero;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutFormulaire statut = StatutFormulaire.BROUILLON;

    /** Structure complète du formulaire en JSON (métadonnées). Stocké en TEXT inline (pas @Lob). */
    @Column(name = "structure_json", length = 1_000_000)
    private String structureJson;

    @Column(length = 1000)
    private String note;

    @Column(name = "publie_le")
    private Instant publieLe;

    @Column(name = "cree_le", nullable = false)
    private Instant creeLe = Instant.now();

    public FormulaireVersion() {}

    public FormulaireVersion(Long formulaireId, int numero, String structureJson, String note) {
        this.formulaireId = formulaireId;
        this.numero = numero;
        this.structureJson = structureJson;
        this.note = note;
    }

    public Long getId() { return id; }
    public Long getFormulaireId() { return formulaireId; }
    public void setFormulaireId(Long formulaireId) { this.formulaireId = formulaireId; }
    public int getNumero() { return numero; }
    public void setNumero(int numero) { this.numero = numero; }
    public StatutFormulaire getStatut() { return statut; }
    public void setStatut(StatutFormulaire statut) { this.statut = statut; }
    public String getStructureJson() { return structureJson; }
    public void setStructureJson(String structureJson) { this.structureJson = structureJson; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public Instant getPublieLe() { return publieLe; }
    public void setPublieLe(Instant publieLe) { this.publieLe = publieLe; }
    public Instant getCreeLe() { return creeLe; }
    public void setCreeLe(Instant creeLe) { this.creeLe = creeLe; }
}
