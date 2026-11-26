package qc.levis.contrat.clm.domain;

import jakarta.persistence.*;
import java.time.Instant;

/** Version d'un modèle de contrat (ST-05) ; les versions d'un même modèle partagent {@code cleModele}. */
@Entity
@Table(name = "clm_modele", uniqueConstraints = @UniqueConstraint(name = "uk_clm_modele_version", columnNames = {"tenant_id", "cle_modele", "version_no"}))
public class ModeleContrat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "cle_modele", nullable = false, length = 40)
    private String cleModele;

    @Column(name = "version_no", nullable = false)
    private int numero;

    @Column(nullable = false, length = 200)
    private String nom;

    @Column(length = 80)
    private String categorie;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private Modele.Statut statut = Modele.Statut.BROUILLON;

    @Column(nullable = false, length = 150)
    private String proprietaire;

    /** Groupes avec qui le modèle est partagé, séparés par des virgules ; vide = tout le tenant. */
    @Column(length = 1000)
    private String groupes;

    @Column(length = 200_000)
    private String corps;

    @Column(name = "champs_json", length = 100_000)
    private String champsJson = "[]";

    @Column(name = "clauses_json", length = 200_000)
    private String clausesJson = "[]";

    /** Parcours par défaut des trousses issues du modèle ; verrouillé avec lui à la publication (UI-11). */
    @Column(name = "parcours_json", length = 50_000)
    private String parcoursJson = "[]";

    /** Champs modifiables une fois le parcours lancé, et effet sur les décisions obtenues (Annexe 10, AF-34). */
    @Column(name = "modifications_json", length = 20_000)
    private String modificationsJson = "[]";

    @Column(name = "cree_le", nullable = false)
    private Instant creeLe;

    @Column(name = "maj_le", nullable = false)
    private Instant majLe;

    @Version
    private long verrou;

    protected ModeleContrat() {
    }

    public ModeleContrat(Long tenantId, String cleModele, int numero, String nom, String proprietaire, Instant maintenant) {
        this.tenantId = tenantId;
        this.cleModele = cleModele;
        this.numero = numero;
        this.nom = nom;
        this.proprietaire = proprietaire;
        this.creeLe = maintenant;
        this.majLe = maintenant;
    }

    public void exigerBrouillon() {
        if (statut != Modele.Statut.BROUILLON) {
            throw new IllegalStateException("Seul un brouillon est modifiable (statut " + statut + ").");
        }
    }

    public void changerStatut(Modele.Statut cible, Instant maintenant) {
        this.statut = statut.vers(cible);
        this.majLe = maintenant;
    }

    public void modifier(String nom, String categorie, String groupes, String corps, Contenu contenu, Instant maintenant) {
        this.nom = nom;
        this.categorie = categorie;
        this.groupes = groupes;
        this.corps = corps;
        this.champsJson = contenu.champsJson();
        this.clausesJson = contenu.clausesJson();
        this.parcoursJson = contenu.parcoursJson();
        this.majLe = maintenant;
    }

    /** Règles de modification en cours de traitement, sérialisées en JSON ; versionnées avec le reste du brouillon. */
    public void definirModificationsEnCours(String json, Instant maintenant) {
        this.modificationsJson = json;
        this.majLe = maintenant;
    }

    /** Parties structurées d'un modèle, sérialisées en JSON. */
    public record Contenu(String champsJson, String clausesJson, String parcoursJson) {
    }

    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public String getCleModele() { return cleModele; }
    public int getNumero() { return numero; }
    public String getNom() { return nom; }
    public String getCategorie() { return categorie; }
    public Modele.Statut getStatut() { return statut; }
    public String getProprietaire() { return proprietaire; }
    public String getGroupes() { return groupes; }
    public String getCorps() { return corps; }
    public String getChampsJson() { return champsJson; }
    public String getClausesJson() { return clausesJson; }
    public String getParcoursJson() { return parcoursJson; }
    public String getModificationsJson() { return modificationsJson; }
    public Instant getMajLe() { return majLe; }
    public long getVerrou() { return verrou; }
}
