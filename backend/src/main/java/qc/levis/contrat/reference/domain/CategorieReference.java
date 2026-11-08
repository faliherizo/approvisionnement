package qc.levis.contrat.reference.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Liste de référence déclarée par un module. Elle n'est servie qu'aux organisations qui ont souscrit ce module ; un
 * libellé de détail, s'il est présent, invite à compléter chaque valeur (ex. mots-clés de classification).
 */
@Entity
@Table(name = "reference_categorie")
public class CategorieReference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String module;

    @Column(nullable = false)
    private String libelle;

    private String aide;

    @Column(name = "libelle_detail")
    private String libelleDetail;

    private int ordre;

    protected CategorieReference() {
    }

    public CategorieReference(String code, String module) {
        this.code = code;
        this.module = module;
    }

    /** Une nouvelle déclaration du module remplace la description : la liste suit le code qui la déclare. */
    public void decrire(String module, String libelle, String aide, String libelleDetail, int ordre) {
        this.module = module;
        this.libelle = libelle;
        this.aide = aide;
        this.libelleDetail = libelleDetail;
        this.ordre = ordre;
    }

    public boolean avecDetail() {
        return libelleDetail != null && !libelleDetail.isBlank();
    }

    public String getCode() { return code; }
    public String getModule() { return module; }
    public String getLibelle() { return libelle; }
    public String getAide() { return aide; }
    public String getLibelleDetail() { return libelleDetail; }
    public int getOrdre() { return ordre; }
}
