package qc.levis.contrat.clm.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Règle du calendrier de conservation de l'organisation (Cahier § 1.4.11 et § 1.4.12, Loi sur les archives) : pour un
 * type de contrat — ou par défaut, sans type — durée de conservation après la clôture et sort final. Sans règle
 * applicable, rien n'est jamais détruit.
 */
@Entity
@Table(name = "clm_regle_conservation")
public class RegleConservation {

    /** Sort final à l'échéance : destruction, versement aux archives (puis destruction), ou conservation permanente. */
    public enum SortFinal { DETRUIRE, VERSER, CONSERVER }

    public static final int DUREE_MAX_ANS = 99;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    /** Code de la liste des types de contrat ; null : règle par défaut de l'organisation. */
    @Column(name = "type_contrat", length = 80)
    private String typeContrat;

    @Column(name = "duree_ans", nullable = false)
    private int dureeAns;

    @Enumerated(EnumType.STRING)
    @Column(name = "sort_final", nullable = false, length = 12)
    private SortFinal sortFinal;

    @Column(name = "modifie_par", nullable = false, length = 200)
    private String modifiePar;

    @Column(name = "modifie_le", nullable = false)
    private Instant modifieLe;

    protected RegleConservation() {
    }

    public RegleConservation(Long tenantId, String typeContrat) {
        this.tenantId = tenantId;
        this.typeContrat = typeContrat == null || typeContrat.isBlank() ? null : typeContrat.strip();
    }

    public void definir(int dureeAns, SortFinal sortFinal, String par, Instant le) {
        if (dureeAns < 1 || dureeAns > DUREE_MAX_ANS) {
            throw new IllegalArgumentException("Durée de conservation : de 1 à " + DUREE_MAX_ANS + " ans.");
        }
        if (sortFinal == null) {
            throw new IllegalArgumentException("Sort final requis : DETRUIRE, VERSER ou CONSERVER.");
        }
        this.dureeAns = dureeAns;
        this.sortFinal = sortFinal;
        this.modifiePar = par;
        this.modifieLe = le;
    }

    /** Date à partir de laquelle le sort final s'applique ; jamais pour une conservation permanente. */
    public LocalDate echeance(Instant clotureLe, ZoneId fuseau) {
        if (sortFinal == SortFinal.CONSERVER || clotureLe == null) {
            return null;
        }
        return LocalDate.ofInstant(clotureLe, fuseau).plusYears(dureeAns);
    }

    public Long getId() { return id; }
    public String getTypeContrat() { return typeContrat; }
    public int getDureeAns() { return dureeAns; }
    public SortFinal getSortFinal() { return sortFinal; }
    public String getModifiePar() { return modifiePar; }
    public Instant getModifieLe() { return modifieLe; }
}
