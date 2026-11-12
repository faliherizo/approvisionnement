package qc.levis.contrat.clm.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.Locale;

/** Appartenance d'un compte de l'organisation, désigné par son courriel, à un groupe du module. */
@Entity
@Table(name = "clm_groupe_membre", uniqueConstraints = @UniqueConstraint(name = "uk_clm_groupe_membre", columnNames = {"groupe_id", "courriel"}))
public class MembreGroupeClm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "groupe_id", nullable = false)
    private Long groupeId;

    @Column(name = "groupe_code", nullable = false, length = 60)
    private String groupeCode;

    @Column(nullable = false, length = 200)
    private String courriel;

    protected MembreGroupeClm() {
    }

    public MembreGroupeClm(GroupeClm groupe, String courriel) {
        this.tenantId = groupe.getTenantId();
        this.groupeId = groupe.getId();
        this.groupeCode = groupe.getCode();
        this.courriel = courriel.strip().toLowerCase(Locale.ROOT);
    }

    public Long getId() { return id; }
    public Long getGroupeId() { return groupeId; }
    public String getGroupeCode() { return groupeCode; }
    public String getCourriel() { return courriel; }
}
