package qc.levis.contrat.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Duration;
import java.time.Instant;

/** Politique de session propre à une organisation (ST-01) ; à défaut, la politique de la plateforme s'applique. */
@Entity
@Table(name = "politique_session")
public class PolitiqueSession {

    @Id
    @Column(name = "tenant_id")
    private Long tenantId;

    @Column(name = "inactivite_minutes", nullable = false)
    private int inactiviteMinutes;

    @Column(name = "duree_max_minutes", nullable = false)
    private int dureeMaxMinutes;

    @Column(name = "modifie_par", nullable = false, length = 200)
    private String modifiePar;

    @Column(name = "modifie_le", nullable = false)
    private Instant modifieLe;

    protected PolitiqueSession() {
    }

    public PolitiqueSession(Long tenantId) {
        this.tenantId = tenantId;
    }

    /** Fixe les limites (validées par {@link LimitesSession}) et en garde l'auteur. */
    public void definir(LimitesSession limites, String par, Instant le) {
        this.inactiviteMinutes = (int) limites.inactivite().toMinutes();
        this.dureeMaxMinutes = (int) limites.dureeMax().toMinutes();
        this.modifiePar = par;
        this.modifieLe = le;
    }

    public LimitesSession limites() {
        return new LimitesSession(Duration.ofMinutes(inactiviteMinutes), Duration.ofMinutes(dureeMaxMinutes));
    }

    public Long getTenantId() { return tenantId; }
    public String getModifiePar() { return modifiePar; }
    public Instant getModifieLe() { return modifieLe; }
}
