package qc.levis.contrat.clm.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

/**
 * Version numérotée d'une négociation (RST-55) : les clauses telles qu'à la fin d'un tour, figées. Leur empreinte SHA-256,
 * aussi consignée au journal scellé de la trousse, rend toute altération ultérieure décelable.
 */
@Entity
@Table(name = "clm_negociation_version",
        uniqueConstraints = @UniqueConstraint(name = "uk_clm_nego_version", columnNames = {"negociation_id", "numero"}))
public class VersionNegociation {

    /** Contenu d'une version : son numéro, ses clauses sérialisées et ce qui l'a produite. */
    public record Contenu(int numero, String clausesJson, String motif) {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "negociation_id", nullable = false)
    private Long negociationId;

    @Column(nullable = false)
    private int numero;

    @Column(name = "clauses_json", nullable = false, length = 200_000)
    private String clausesJson;

    @Column(nullable = false, length = 64)
    private String empreinte;

    @Column(nullable = false, length = 150)
    private String auteur;

    @Column(nullable = false, length = 500)
    private String motif;

    @Column(name = "cree_le", nullable = false)
    private Instant creeLe;

    protected VersionNegociation() {
    }

    public VersionNegociation(Negociation n, Contenu c, String auteur, Instant le) {
        this.tenantId = n.getTenantId();
        this.negociationId = n.getId();
        this.numero = c.numero();
        this.clausesJson = c.clausesJson();
        this.empreinte = empreinteDe(c.clausesJson());
        this.auteur = auteur;
        this.motif = c.motif();
        this.creeLe = le;
    }

    /** Les clauses relues donnent toujours l'empreinte enregistrée à la création de la version. */
    public boolean estIntegre() {
        return empreinteDe(clausesJson).equals(empreinte);
    }

    private static String empreinteDe(String json) {
        return ChaineScellee.sha256(json.getBytes(StandardCharsets.UTF_8));
    }

    public Long getId() { return id; }
    public Long getNegociationId() { return negociationId; }
    public int getNumero() { return numero; }
    public String getClausesJson() { return clausesJson; }
    public String getEmpreinte() { return empreinte; }
    public String getAuteur() { return auteur; }
    public String getMotif() { return motif; }
    public Instant getCreeLe() { return creeLe; }
}
