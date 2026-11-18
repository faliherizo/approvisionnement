package qc.levis.contrat.clm.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Compteur des références de trousses d'une organisation, par préfixe et par année. Lu sous verrou exclusif : deux
 * créations simultanées ne reçoivent jamais le même numéro, et la numérotation ne relit pas toutes les trousses.
 */
@Entity
@Table(name = "clm_compteur_reference", uniqueConstraints = @UniqueConstraint(name = "uk_clm_compteur_reference",
        columnNames = {"tenant_id", "prefixe", "annee"}))
public class CompteurReference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(nullable = false, length = 20)
    private String prefixe;

    @Column(nullable = false)
    private int annee;

    @Column(nullable = false)
    private long dernier;

    protected CompteurReference() {
    }

    /** @param dernier dernier numéro déjà attribué (0 pour une année neuve) */
    public CompteurReference(Long tenantId, String prefixe, int annee, long dernier) {
        this.tenantId = tenantId;
        this.prefixe = prefixe;
        this.annee = annee;
        this.dernier = dernier;
    }

    /** Numéro suivant, sous la forme {@code PREFIXE-AAAA-NNNNN}. */
    public String suivante() {
        dernier++;
        return String.format("%s-%d-%05d", prefixe, annee, dernier);
    }

    public long getDernier() { return dernier; }
}
