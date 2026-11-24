package qc.levis.contrat.interop.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Échange hérité de la plateforme précédente (CHU-8.04, ADR-021) : un code que le système externe appelle, le
 * formulaire qui reçoit ses fiches, et la correspondance de ses noms de champs.
 */
@Entity
@Table(name = "interop_echange")
public class EchangeHerite {

    private static final Pattern CODE = Pattern.compile("[a-z0-9][a-z0-9-]{1,59}");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(nullable = false, length = 60)
    private String code;

    @Column(name = "formulaire_id", nullable = false)
    private Long formulaireId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "interop_echange_champ", joinColumns = @JoinColumn(name = "echange_id"))
    @MapKeyColumn(name = "ancien", length = CorrespondanceEchange.NOM_MAX)
    @Column(name = "cle", length = CorrespondanceEchange.NOM_MAX, nullable = false)
    private Map<String, String> champs = new LinkedHashMap<>();

    @Column(nullable = false)
    private boolean actif = true;

    @Column(name = "cree_par", nullable = false, length = 200)
    private String creePar;

    @Column(name = "cree_le", nullable = false)
    private Instant creeLe;

    protected EchangeHerite() {
    }

    public static EchangeHerite declarer(Long tenantId, String code, Long formulaireId, CorrespondanceEchange correspondance,
                                         String par, Instant le) {
        String c = code == null ? "" : code.strip();
        if (!CODE.matcher(c).matches()) {
            throw new CorrespondanceEchange.CorrespondanceInvalide(
                    "Code d'échange : 2 à 60 caractères, minuscules, chiffres et tirets (ex. : inscriptions-formation).");
        }
        EchangeHerite e = new EchangeHerite();
        e.tenantId = tenantId;
        e.code = c;
        e.formulaireId = formulaireId;
        e.champs = new LinkedHashMap<>(correspondance.table());
        e.creePar = par;
        e.creeLe = le;
        return e;
    }

    public CorrespondanceEchange correspondance() {
        return CorrespondanceEchange.relue(champs);
    }

    public void desactiver() {
        if (!actif) {
            throw new IllegalStateException("Échange déjà désactivé.");
        }
        actif = false;
    }

    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public String getCode() { return code; }
    public Long getFormulaireId() { return formulaireId; }
    public boolean isActif() { return actif; }
    public String getCreePar() { return creePar; }
    public Instant getCreeLe() { return creeLe; }
}
