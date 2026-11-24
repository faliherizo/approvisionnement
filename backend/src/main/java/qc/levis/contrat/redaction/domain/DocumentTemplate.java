package qc.levis.contrat.redaction.domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import qc.levis.contrat.commun.domain.Enums;

/** Template de document nommé : type, mode et liste de clauses pré-sélectionnées. */
@Entity
// Code unique dans son organisation (migration 073) : chaque organisation a sa bibliothèque de départ.
@Table(uniqueConstraints = @UniqueConstraint(name = "uq_document_template_tenant_code", columnNames = {"tenant_id", "code"}))
public class DocumentTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String nom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Enums.TypeContrat type;

    @Enumerated(EnumType.STRING)
    private Enums.ModeSollicitation modeSollicitation;

    /** Classement par type d'acquisition (art. 7.1) : biens, services, travaux, TI… */
    @Enumerated(EnumType.STRING)
    @Column(name = "categorie_acquisition")
    private Enums.TypeAcquisition categorieAcquisition;

    @Column(length = 1000)
    private String description;

    @Column(name = "tenant_id")
    private Long tenantId;

    /** Titre par défaut proposé au document instancié (pré-remplissage). */
    @Column(name = "titre_par_defaut")
    private String titreParDefaut;

    /** Contexte / contenu par défaut du document (WYSIWYG). */
    @Column(length = 8000)
    private String contexte;

    /** Gabarit de rendu associé (design : police, couverture, format) — code de {@code Gabarit}. */
    @Column(name = "gabarit_code")
    private String gabaritCode;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "document_template_clause", joinColumns = @JoinColumn(name = "template_id"))
    @Column(name = "clause_code")
    private List<String> clauseCodes = new ArrayList<>();

    /** Titres d'annexes créées automatiquement à l'instanciation. */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "document_template_annexe", joinColumns = @JoinColumn(name = "template_id"))
    @Column(name = "annexe_titre")
    private List<String> annexes = new ArrayList<>();

    public Long getId() { return id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public Enums.TypeContrat getType() { return type; }
    public void setType(Enums.TypeContrat type) { this.type = type; }
    public Enums.ModeSollicitation getModeSollicitation() { return modeSollicitation; }
    public void setModeSollicitation(Enums.ModeSollicitation m) { this.modeSollicitation = m; }
    public Enums.TypeAcquisition getCategorieAcquisition() { return categorieAcquisition; }
    public void setCategorieAcquisition(Enums.TypeAcquisition c) { this.categorieAcquisition = c; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public String getTitreParDefaut() { return titreParDefaut; }
    public void setTitreParDefaut(String v) { this.titreParDefaut = v; }
    public String getContexte() { return contexte; }
    public void setContexte(String contexte) { this.contexte = contexte; }
    public String getGabaritCode() { return gabaritCode; }
    public void setGabaritCode(String gabaritCode) { this.gabaritCode = gabaritCode; }
    public List<String> getClauseCodes() { return clauseCodes; }
    public void setClauseCodes(List<String> c) { this.clauseCodes = c; }
    public List<String> getAnnexes() { return annexes; }
    public void setAnnexes(List<String> annexes) { this.annexes = annexes; }
}
