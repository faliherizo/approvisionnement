package qc.levis.contrat.clm.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

/**
 * Clé de données dédiée à une organisation (Annexe 10, SEC-28 ; ADR-037), une version par rotation. Elle chiffre le contenu
 * des documents de l'organisation et n'est jamais stockée en clair : seule son enveloppe par la clé maîtresse l'est. Une
 * seule version est active à la fois ; les versions retirées restent lisibles pour les documents qui en dépendent encore.
 */
@Entity
@Table(name = "clm_cle_organisation", uniqueConstraints = @UniqueConstraint(name = "uk_clm_cle_org_version",
        columnNames = {"tenant_id", "version"}))
public class CleOrganisation {

    public enum Statut { ACTIVE, RETIREE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(nullable = false)
    private int version;

    @Column(name = "cle_maitresse", nullable = false, length = CleEnveloppee.CLE_MAITRESSE_MAX)
    private String cleMaitresse;

    @Column(name = "cle_enveloppee", nullable = false, length = CleEnveloppee.VALEUR_MAX)
    private String cleEnveloppee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private Statut statut = Statut.ACTIVE;

    @Column(name = "cree_le", nullable = false)
    private Instant creeLe;

    @Column(name = "retiree_le")
    private Instant retireeLe;

    protected CleOrganisation() {
    }

    public static CleOrganisation creer(Long tenantId, int version, CleEnveloppee cle, Instant le) {
        if (version < 1) {
            throw new IllegalArgumentException("Version de clé invalide.");
        }
        CleOrganisation c = new CleOrganisation();
        c.tenantId = tenantId;
        c.version = version;
        c.cleMaitresse = cle.cleMaitresse();
        c.cleEnveloppee = cle.valeur();
        c.creeLe = le;
        return c;
    }

    /** Rotation : la version cesse de chiffrer les nouveaux documents ; elle déchiffre encore les anciens. */
    public void retirer(Instant le) {
        if (statut == Statut.RETIREE) {
            throw new IllegalStateException("Version de clé déjà retirée.");
        }
        statut = Statut.RETIREE;
        retireeLe = le;
    }

    /** Rotation de la clé maîtresse : la même clé de données, enveloppée par la nouvelle clé maîtresse. */
    public void reenvelopper(CleEnveloppee nouvelle) {
        cleMaitresse = nouvelle.cleMaitresse();
        cleEnveloppee = nouvelle.valeur();
    }

    public boolean estActive() {
        return statut == Statut.ACTIVE;
    }

    public CleEnveloppee enveloppe() {
        return new CleEnveloppee(cleMaitresse, cleEnveloppee);
    }

    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public int getVersion() { return version; }
    public String getCleMaitresse() { return cleMaitresse; }
    public Statut getStatut() { return statut; }
    public Instant getCreeLe() { return creeLe; }
    public Instant getRetireeLe() { return retireeLe; }
}
