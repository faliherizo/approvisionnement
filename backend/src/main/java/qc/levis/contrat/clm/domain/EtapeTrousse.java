package qc.levis.contrat.clm.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/**
 * Étape instanciée du parcours d'une trousse. Les affectations sont modifiables tant que l'étape n'est pas
 * décidée, ce qui permet réassignation, ajout et retrait d'intervenants sans redémarrer le parcours (ST-08).
 */
@Entity
@Table(name = "clm_etape", indexes = {
        @Index(name = "ix_clm_etape_trousse", columnList = "tenant_id,trousse_id"),
        @Index(name = "ix_clm_etape_statut", columnList = "statut")})
public class EtapeTrousse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "trousse_id", nullable = false)
    private Long trousseId;

    @Column(name = "cle_etape", nullable = false, length = 60)
    private String cle;

    @Column(nullable = false, length = 150)
    private String libelle;

    @Column(nullable = false)
    private int phase;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private Parcours.TypeEtape type;

    @Column(name = "affectations_json", nullable = false, length = 10_000)
    private String affectationsJson;

    @Column(nullable = false)
    private int quorum;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private Parcours.PolitiqueRejet politique;

    @Column(name = "delai_jours")
    private Integer delaiJours;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private Parcours.StatutEtape statut = Parcours.StatutEtape.EN_ATTENTE;

    @Column(name = "activee_le")
    private Instant activeeLe;

    private Instant echeance;

    @Column(name = "terminee_le")
    private Instant termineeLe;

    @Column(name = "rappels_envoyes", nullable = false)
    private int rappelsEnvoyes;

    @Column(name = "dernier_rappel")
    private Instant dernierRappel;

    @Column(name = "retard_signale", nullable = false)
    private boolean retardSignale;

    protected EtapeTrousse() {
    }

    public EtapeTrousse(Long tenantId, Long trousseId, Parcours.DefinitionEtape d, String affectationsJson) {
        this.tenantId = tenantId;
        this.trousseId = trousseId;
        this.cle = d.cle();
        this.libelle = d.libelle() == null || d.libelle().isBlank() ? d.cle() : d.libelle();
        this.phase = d.phase();
        this.type = d.type();
        this.affectationsJson = affectationsJson;
        this.quorum = d.quorum();
        this.politique = d.politique();
        this.delaiJours = d.delaiJours();
    }

    public void activer(Instant maintenant) {
        this.statut = Parcours.StatutEtape.ACTIVE;
        this.activeeLe = maintenant;
        this.echeance = Relances.echeance(maintenant, delaiJours);
        this.rappelsEnvoyes = 0;
        this.dernierRappel = null;
        this.retardSignale = false;
    }

    public void terminer(Parcours.StatutEtape resultat, Instant maintenant) {
        this.statut = resultat;
        this.termineeLe = maintenant;
    }

    public void reinitialiser() {
        this.statut = Parcours.StatutEtape.EN_ATTENTE;
        this.activeeLe = null;
        this.echeance = null;
        this.termineeLe = null;
    }

    public void exigerModifiable() {
        if (statut != Parcours.StatutEtape.EN_ATTENTE && statut != Parcours.StatutEtape.ACTIVE) {
            throw new IllegalStateException("Étape déjà terminée : " + libelle);
        }
    }

    public void noterRappel(Instant maintenant) {
        this.rappelsEnvoyes++;
        this.dernierRappel = maintenant;
    }

    public void signalerRetard() {
        this.retardSignale = true;
    }

    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public Long getTrousseId() { return trousseId; }
    public String getCle() { return cle; }
    public String getLibelle() { return libelle; }
    public int getPhase() { return phase; }
    public Parcours.TypeEtape getType() { return type; }
    public String getAffectationsJson() { return affectationsJson; }
    public void setAffectationsJson(String affectationsJson) { this.affectationsJson = affectationsJson; }
    public int getQuorum() { return quorum; }
    public void setQuorum(int quorum) { this.quorum = quorum; }
    public Parcours.PolitiqueRejet getPolitique() { return politique; }
    public Integer getDelaiJours() { return delaiJours; }
    public Parcours.StatutEtape getStatut() { return statut; }
    public Instant getActiveeLe() { return activeeLe; }
    public Instant getEcheance() { return echeance; }
    public Instant getTermineeLe() { return termineeLe; }
    public int getRappelsEnvoyes() { return rappelsEnvoyes; }
    public Instant getDernierRappel() { return dernierRappel; }
    public boolean isRetardSignale() { return retardSignale; }
}
