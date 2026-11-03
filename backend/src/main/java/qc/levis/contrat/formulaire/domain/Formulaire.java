package qc.levis.contrat.formulaire.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Formulaire (ou application Web) d'un tenant. L'entité ne porte AUCUN champ métier : la structure
 * (pages, champs, types, règles) vit dans {@link FormulaireVersion#getStructureJson()} (métadonnées).
 * {@code parentId} permet les formulaires liés parent-enfant (processus multi-niveaux).
 */
@Entity
@Table(name = "formulaire",
        uniqueConstraints = @UniqueConstraint(name = "uq_formulaire_tenant_code", columnNames = {"tenant_id", "code"}))
public class Formulaire {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(nullable = false, length = 80)
    private String code;

    @Column(nullable = false, length = 300)
    private String nom;

    /**
     * Domaine métier (code de donnée de référence DOMAINE : {@code RH}, {@code APPROVISIONNEMENT}, …).
     * Null = transversal / non classé. Sert au cloisonnement RH/Approvisionnement et au filtrage des
     * catalogues (maquettes §4.2, architecture §5.2 DOMAIN) — aucun domaine n'est figé dans le code.
     */
    @Column(length = 40)
    private String domaine;

    @Column(name = "parent_id")
    private Long parentId;

    /**
     * Durée de conservation des fiches de ce formulaire, en jours (politique de rétention — §6 de l'avis).
     * Null = pas de politique définie ; la valeur réelle vient du calendrier de conservation du CHU.
     */
    @Column(name = "conservation_jours")
    private Integer conservationJours;

    @Column(nullable = false)
    private boolean actif = true;

    @Column(name = "cree_le", nullable = false)
    private Instant creeLe = Instant.now();

    public Formulaire() {}

    public Formulaire(Long tenantId, String code, String nom, Long parentId) {
        this.tenantId = tenantId;
        this.code = code;
        this.nom = nom;
        this.parentId = parentId;
    }

    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getDomaine() { return domaine; }
    public void setDomaine(String domaine) { this.domaine = domaine; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public Integer getConservationJours() { return conservationJours; }
    public void setConservationJours(Integer conservationJours) { this.conservationJours = conservationJours; }
    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }
    public Instant getCreeLe() { return creeLe; }
    public void setCreeLe(Instant creeLe) { this.creeLe = creeLe; }
}
