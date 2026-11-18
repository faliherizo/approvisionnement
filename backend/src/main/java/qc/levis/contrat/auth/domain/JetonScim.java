package qc.levis.contrat.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Jeton porteur du point SCIM d'une organisation : seule son empreinte SHA-256 est conservée ; le jeton en clair n'est
 * remis qu'une fois, à sa création, pour être saisi dans Saviynt. En créer un nouveau révoque le précédent.
 */
@Entity
@Table(name = "annuaire_jeton_scim")
public class JetonScim {

    @Id
    @Column(name = "tenant_id")
    private Long tenantId;

    @Column(nullable = false, length = 64)
    private String empreinte;

    @Column(name = "cree_par", nullable = false, length = 200)
    private String creePar;

    @Column(name = "cree_le", nullable = false)
    private Instant creeLe;

    protected JetonScim() {
    }

    public JetonScim(Long tenantId, String empreinte, String creePar, Instant creeLe) {
        this.tenantId = tenantId;
        remplacer(empreinte, creePar, creeLe);
    }

    public final void remplacer(String empreinte, String par, Instant le) {
        this.empreinte = empreinte;
        this.creePar = par;
        this.creeLe = le;
    }

    public Long getTenantId() { return tenantId; }
    public String getEmpreinte() { return empreinte; }
    public String getCreePar() { return creePar; }
    public Instant getCreeLe() { return creeLe; }
}
