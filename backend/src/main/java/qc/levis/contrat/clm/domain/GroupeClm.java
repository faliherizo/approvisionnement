package qc.levis.contrat.clm.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.Locale;

/**
 * Groupe d'utilisateurs du module (Cahier § 1.4.7 et § 1.4.12) : partage des modèles, affectation d'une étape, et — quand
 * son code est celui d'une unité administrative — visibilité des trousses de l'unité. Administré dans l'espace du module,
 * sans dépendre de la sécurité avancée.
 */
@Entity
@Table(name = "clm_groupe", uniqueConstraints = @UniqueConstraint(name = "uk_clm_groupe_code", columnNames = {"tenant_id", "code"}))
public class GroupeClm {

    public static final int LIBELLE_MAX = 120;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(nullable = false, length = 60)
    private String code;

    @Column(nullable = false, length = LIBELLE_MAX)
    private String libelle;

    @Column(name = "cree_par", nullable = false, length = 200)
    private String creePar;

    @Column(name = "cree_le", nullable = false)
    private Instant creeLe;

    protected GroupeClm() {
    }

    /** Code en majuscules, chiffres et soulignés : il sert d'identifiant dans les affectations et les modèles. */
    public static GroupeClm creer(Long tenantId, String code, String libelle, String par, Instant le) {
        String c = code == null ? "" : code.strip().toUpperCase(Locale.ROOT);
        if (!c.matches("[A-Z][A-Z0-9_]{0,59}")) {
            throw new IllegalArgumentException("Code du groupe : lettre puis lettres, chiffres ou soulignés (60 au plus).");
        }
        GroupeClm g = new GroupeClm();
        g.tenantId = tenantId;
        g.code = c;
        g.libelle = libelleValide(libelle);
        g.creePar = par;
        g.creeLe = le;
        return g;
    }

    /** Le code, repris par les parcours et le partage des modèles, ne change pas ; seul le libellé se renomme. */
    public void renommer(String libelle) {
        this.libelle = libelleValide(libelle);
    }

    private static String libelleValide(String libelle) {
        String l = libelle == null ? "" : libelle.strip();
        if (l.isEmpty() || l.length() > LIBELLE_MAX) {
            throw new IllegalArgumentException("Libellé du groupe : 1 à " + LIBELLE_MAX + " caractères.");
        }
        return l;
    }

    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public String getCode() { return code; }
    public String getLibelle() { return libelle; }
    public String getCreePar() { return creePar; }
    public Instant getCreeLe() { return creeLe; }
}
