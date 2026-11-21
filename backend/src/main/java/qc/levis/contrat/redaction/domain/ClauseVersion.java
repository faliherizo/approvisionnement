package qc.levis.contrat.redaction.domain;

import jakarta.persistence.*;
import java.time.Instant;
import qc.levis.contrat.commun.domain.Enums;

/**
 * Version consultable d'une clause (Lévis EF1, C-03) : instantané immuable du texte et de ses attributs, avec son auteur,
 * sa date et le motif qui l'a produite. Le registre est en ajout seul ; restaurer une version en crée une nouvelle.
 */
@Entity
@Table(name = "clause_version")
public class ClauseVersion {

    public enum Motif { CREATION, MODIFICATION, RESTAURATION }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, updatable = false) private Long clauseId;
    @Column(updatable = false) private Long tenantId;
    @Column(nullable = false, updatable = false) private int numero;
    @Column(nullable = false, updatable = false, length = 300) private String titre;
    @Enumerated(EnumType.STRING) @Column(nullable = false, updatable = false, length = 40) private Enums.ClauseCategorie categorie;
    @Column(nullable = false, updatable = false, length = 20000) private String corps;
    @Column(updatable = false, length = 40) private String article;
    @Column(updatable = false, length = 200) private String section;
    @Column(nullable = false, updatable = false) private boolean normative;
    @Enumerated(EnumType.STRING) @Column(nullable = false, updatable = false, length = 20) private Motif motif;
    @Column(updatable = false) private Integer versionRestauree;
    @Column(nullable = false, length = 150) private String auteur;
    @Column(nullable = false, updatable = false) private Instant creeLe;

    protected ClauseVersion() { }

    /** Fige l'état courant de la clause sous son numéro de version. */
    public static ClauseVersion de(Clause c, Motif motif, Integer versionRestauree, String auteur, Instant le) {
        ClauseVersion v = new ClauseVersion();
        v.clauseId = c.getId();
        v.tenantId = c.getTenantId();
        v.numero = c.getVersion();
        v.titre = c.getTitre();
        v.categorie = c.getCategorie();
        v.corps = c.getCorps();
        v.article = c.getNumero();
        v.section = c.getSection();
        v.normative = c.isNormative();
        v.motif = motif;
        v.versionRestauree = versionRestauree;
        v.auteur = auteur;
        v.creeLe = le;
        return v;
    }

    /** Reporte le texte et les attributs de cette version sur la clause ; le numéro de version de la clause avance. */
    public void restaurerDans(Clause c, Instant le) {
        c.setTitre(titre);
        c.setCategorie(categorie);
        c.setCorps(corps);
        c.setNumero(article);
        c.setSection(section);
        c.setNormative(normative);
        c.setVersion(c.getVersion() + 1);
        c.setMajLe(le);
    }

    /** Loi 25 : seule l'attribution change ; le texte de la version reste intact. */
    public void pseudonymiserAuteur(String pseudonyme) {
        auteur = pseudonyme;
    }

    public Long getId() { return id; }
    public Long getClauseId() { return clauseId; }
    public Long getTenantId() { return tenantId; }
    public int getNumero() { return numero; }
    public String getTitre() { return titre; }
    public Enums.ClauseCategorie getCategorie() { return categorie; }
    public String getCorps() { return corps; }
    public String getArticle() { return article; }
    public String getSection() { return section; }
    public boolean isNormative() { return normative; }
    public Motif getMotif() { return motif; }
    public Integer getVersionRestauree() { return versionRestauree; }
    public String getAuteur() { return auteur; }
    public Instant getCreeLe() { return creeLe; }
}
