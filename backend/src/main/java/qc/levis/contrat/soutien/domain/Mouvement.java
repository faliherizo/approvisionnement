package qc.levis.contrat.soutien.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Mouvement d'une banque d'heures : achat d'heures, ou consommation imputée à une catégorie de service. Les heures se
 * comptent au quart d'heure. Un mouvement ne se modifie pas : une erreur se corrige par un mouvement inverse (traçabilité).
 */
@Entity
@Table(name = "soutien_mouvement")
public class Mouvement {

    public enum Type { ACHAT, CONSOMMATION, CORRECTION }

    static final BigDecimal QUART = new BigDecimal("0.25");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, updatable = false) private Long banqueId;
    @Enumerated(EnumType.STRING) @Column(nullable = false, updatable = false, length = 20) private Type type;
    @Enumerated(EnumType.STRING) @Column(updatable = false, length = 20) private CategorieService categorie;
    @Column(nullable = false, updatable = false, precision = 9, scale = 2) private BigDecimal heures;
    @Column(name = "date_mouvement", nullable = false, updatable = false) private LocalDate date;
    @Column(nullable = false, updatable = false, length = 500) private String description;
    @Column(updatable = false, length = 150) private String intervenant;
    @Column(nullable = false, updatable = false, length = 150) private String saisiPar;
    @Column(nullable = false, updatable = false) private Instant saisiLe;

    protected Mouvement() { }

    private Mouvement(Long banqueId, Type type, CategorieService categorie, BigDecimal heures, LocalDate date, String description,
                      String intervenant, String saisiPar, Instant saisiLe) {
        this.banqueId = banqueId;
        this.type = type;
        this.categorie = categorie;
        this.heures = heures;
        this.date = date;
        this.description = description;
        this.intervenant = intervenant;
        this.saisiPar = saisiPar;
        this.saisiLe = saisiLe;
    }

    /** Achat d'heures (heures positives). */
    public static Mouvement achat(Long banqueId, BigDecimal heures, LocalDate date, String description, String par, Instant le) {
        return new Mouvement(banqueId, Type.ACHAT, null, exigerQuarts(heures), date, exigerDescription(description), null, par, le);
    }

    /** Consommation d'heures dans une catégorie, par un intervenant. */
    public static Mouvement consommation(Long banqueId, CategorieService categorie, BigDecimal heures, LocalDate date, String description,
                                         String intervenant, String par, Instant le) {
        if (categorie == null) throw new IllegalArgumentException("La catégorie de service est requise.");
        if (intervenant == null || intervenant.isBlank()) throw new IllegalArgumentException("L'intervenant est requis.");
        return new Mouvement(banqueId, Type.CONSOMMATION, categorie, exigerQuarts(heures), date, exigerDescription(description),
                intervenant.strip(), par, le);
    }

    /** Correction d'une consommation saisie en trop : rend des heures à la banque dans la même catégorie. */
    public static Mouvement correction(Long banqueId, CategorieService categorie, BigDecimal heures, LocalDate date, String description,
                                       String par, Instant le) {
        if (categorie == null) throw new IllegalArgumentException("La catégorie de service est requise.");
        return new Mouvement(banqueId, Type.CORRECTION, categorie, exigerQuarts(heures), date, exigerDescription(description), null, par, le);
    }

    static BigDecimal exigerQuarts(BigDecimal heures) {
        if (heures == null || heures.signum() <= 0) throw new IllegalArgumentException("Le nombre d'heures doit être positif.");
        if (heures.remainder(QUART).signum() != 0) throw new IllegalArgumentException("Les heures se comptent au quart d'heure (0,25).");
        return heures.setScale(2);
    }

    private static String exigerDescription(String description) {
        if (description == null || description.isBlank()) throw new IllegalArgumentException("La description du mouvement est requise.");
        return description.strip();
    }

    public Long getId() { return id; }
    public Long getBanqueId() { return banqueId; }
    public Type getType() { return type; }
    public CategorieService getCategorie() { return categorie; }
    public BigDecimal getHeures() { return heures; }
    public LocalDate getDate() { return date; }
    public String getDescription() { return description; }
    public String getIntervenant() { return intervenant; }
    public String getSaisiPar() { return saisiPar; }
    public Instant getSaisiLe() { return saisiLe; }
}
