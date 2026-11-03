package qc.levis.contrat.clm.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/** Maillon du journal scellé d'une trousse (voir {@link ChaineScellee}). Jamais modifié ni supprimé. */
@Entity
@Table(name = "clm_evenement", uniqueConstraints = @UniqueConstraint(name = "uk_clm_evt_seq", columnNames = {"trousse_id", "sequence_no"}),
        indexes = @Index(name = "ix_clm_evt_tenant", columnList = "tenant_id,horodatage"))
public class EvenementTrousse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "trousse_id", nullable = false)
    private Long trousseId;

    @Column(name = "sequence_no", nullable = false)
    private long sequence;

    @Column(nullable = false, length = 60)
    private String type;

    @Column(nullable = false, length = 150)
    private String acteur;

    @Column(nullable = false)
    private Instant horodatage;

    @Column(length = 2000)
    private String detail;

    @Column(name = "empreinte_precedente", nullable = false, length = 64)
    private String empreintePrecedente;

    @Column(nullable = false, length = 64)
    private String empreinte;

    protected EvenementTrousse() {
    }

    public EvenementTrousse(Long tenantId, Long trousseId, long sequence, String type, String acteur, Instant horodatage,
                            String detail, String empreintePrecedente) {
        this.tenantId = tenantId;
        this.trousseId = trousseId;
        this.sequence = sequence;
        this.type = type;
        this.acteur = acteur;
        this.horodatage = horodatage;
        this.detail = detail;
        this.empreintePrecedente = empreintePrecedente;
        this.empreinte = ChaineScellee.empreinte(empreintePrecedente, trousseId, sequence, type, acteur, horodatage, detail);
    }

    public ChaineScellee.Maillon maillon() {
        return new ChaineScellee.Maillon(sequence, type, acteur, horodatage, detail, empreintePrecedente, empreinte);
    }

    public Long getTrousseId() { return trousseId; }
    public long getSequence() { return sequence; }
    public String getType() { return type; }
    public String getActeur() { return acteur; }
    public Instant getHorodatage() { return horodatage; }
    public String getDetail() { return detail; }
    public String getEmpreinte() { return empreinte; }
}
