package qc.levis.contrat.redaction.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import qc.levis.contrat.commun.domain.Enums;

/** Clause de la bibliothèque centralisée. */
@Entity
// Code unique dans son organisation (migration 073) : chaque organisation a sa bibliothèque de départ.
@Table(uniqueConstraints = @UniqueConstraint(name = "uq_clause_tenant_code", columnNames = {"tenant_id", "code"}))
public class Clause {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String titre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Enums.ClauseCategorie categorie = Enums.ClauseCategorie.STANDARD;

    @Column(nullable = false, length = 8000)
    private String corps;

    private int version = 1;
    private boolean actif = true;
    private Instant majLe = Instant.now();

    /** Numéro hiérarchique dans le contrat (ex. « 10.06.01 »). */
    private String numero;

    /** Regroupement / section éditoriale (ex. « Attestations »). */
    private String section;

    /** Clause normative (ex. issue du CCAG global, non modifiable par le tenant). */
    private boolean normative = false;

    @Column(name = "tenant_id")
    private Long tenantId;

    /** Conditions d'inclusion évaluées par le moteur de règles. */
    @OneToMany(mappedBy = "clause", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ClauseCondition> conditions = new ArrayList<>();

    /** Codes de clauses requises (auto-incluses lors de l'assemblage). */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "clause_dependance", joinColumns = @JoinColumn(name = "clause_id"))
    @Column(name = "code_requis")
    private List<String> dependances = new ArrayList<>();

    /** Codes de clauses exclues (jamais assemblées avec celle-ci). */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "clause_exclusion", joinColumns = @JoinColumn(name = "clause_id"))
    @Column(name = "code_exclu")
    private List<String> exclusions = new ArrayList<>();

    public Long getId() { return id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public Enums.ClauseCategorie getCategorie() { return categorie; }
    public void setCategorie(Enums.ClauseCategorie c) { this.categorie = c; }
    public String getCorps() { return corps; }
    public void setCorps(String corps) { this.corps = corps; }
    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }
    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }
    public Instant getMajLe() { return majLe; }
    public void setMajLe(Instant majLe) { this.majLe = majLe; }
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }
    public boolean isNormative() { return normative; }
    public void setNormative(boolean normative) { this.normative = normative; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public List<ClauseCondition> getConditions() { return conditions; }
    public void setConditions(List<ClauseCondition> conditions) { this.conditions = conditions; }
    public List<String> getDependances() { return dependances; }
    public void setDependances(List<String> dependances) { this.dependances = dependances; }
    public List<String> getExclusions() { return exclusions; }
    public void setExclusions(List<String> exclusions) { this.exclusions = exclusions; }

    public void addCondition(ClauseCondition c) {
        c.setClause(this);
        this.conditions.add(c);
    }
}
